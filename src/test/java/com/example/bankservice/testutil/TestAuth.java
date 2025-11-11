package com.example.bankservice.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class TestAuth {
    private TestAuth() {}

    public static String obtainToken(MockMvc mvc) throws Exception {
        MvcResult res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andReturn();
        String body = res.getResponse().getContentAsString();
        ObjectMapper om = new ObjectMapper();
        JsonNode node = om.readTree(body);
        return node.get("token").asText();
    }
}
