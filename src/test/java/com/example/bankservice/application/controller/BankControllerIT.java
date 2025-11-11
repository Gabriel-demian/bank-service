package com.example.bankservice.application.controller;

import com.example.bankservice.testutil.TestAuth;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BankControllerIT {

    @Autowired
    MockMvc mvc;

    @LocalServerPort
    int port;

    ObjectMapper om = new ObjectMapper();
    String token;

    @BeforeEach
    void setUp() throws Exception {
        token = TestAuth.obtainToken(mvc);
    }

    @Test
    void fullCrud_flow_withOptimisticLocking_andProxy() throws Exception {
        // 1) CREATE
        String createBody = """
                {"name":"Banco Patagonia","bic":"PATAGONIA01","country":"AR","routingNumber":"123456789"}
                """;
        MvcResult createdRes = mvc.perform(post("/v1/banks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, startsWith("/v1/banks/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.version", is(0)))
                .andReturn();

        JsonNode createdJson = om.readTree(createdRes.getResponse().getContentAsString());
        UUID id = UUID.fromString(createdJson.get("id").asText());

        // 2) GET
        mvc.perform(get("/v1/banks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bic", is("PATAGONIA01")));

        // 3) LIST (sin filtro)
        mvc.perform(get("/v1/banks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 4) UPDATE OK con If-Match "0"
        String updateBody = """
                {"name":"Banco Patagonia S.A.","bic":"PATAGONIA01","country":"AR","routingNumber":"999999"}
                """;
        MvcResult updateRes = mvc.perform(put("/v1/banks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(HttpHeaders.IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.version", is(1)))
                .andReturn();

        // 5) UPDATE con versión vieja → debe fallar (409/400 según tu handler)
        mvc.perform(put("/v1/banks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(HttpHeaders.IF_MATCH, "\"0\"") // desactualizado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().is4xxClientError()); // idealmente 409

        // 6) PROXY (autollamada a /v1/banks)
        mvc.perform(get("/v1/banks/proxy")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PATAGONIA01")));

        // 7) DELETE
        mvc.perform(delete("/v1/banks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
