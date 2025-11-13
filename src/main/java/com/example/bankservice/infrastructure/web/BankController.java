package com.example.bankservice.infrastructure.web;

import com.example.bankservice.application.dto.BankDto;
import com.example.bankservice.application.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/banks")
@Tag(name = "Banks", description = "CRUD for bank entities + self-call proxy")
public class BankController {

    private static final Logger log = LoggerFactory.getLogger(BankController.class);

    private final BankService service;
    private final WebClient webClient;
    private final BankApiMapper mapper;

    public BankController(BankService service, WebClient webClient, BankApiMapper mapper) {
        this.service = service;
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @Operation(summary = "Create a new bank",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = BankResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Duplicate (BIC or name+country)"),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            })
    @PostMapping
    public ResponseEntity<BankResponse> create(@Valid @RequestBody BankRequest req) {
        log.info("POST /v1/banks name='{}' country='{}' bic='{}'", req.name(), req.country(), req.bic());

        BankDto input = mapper.toDto(req);
        BankDto created = service.create(input);

        URI location = URI.create("/v1/banks/" + created.id());
        log.info("Bank created id={} version={}", created.id(), created.version());

        BankResponse response = mapper.toResponse(created);
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get a bank by id")
    @GetMapping("/{id}")
    public ResponseEntity<BankResponse> get(@PathVariable UUID id) {
        log.debug("GET /v1/banks/{}", id);
        BankDto dto = service.get(id);
        return ResponseEntity.ok(mapper.toResponse(dto));
    }

    @Operation(summary = "List banks")
    @GetMapping
    public ResponseEntity<List<BankResponse>> list(@RequestParam(required = false) String country) {
        log.debug("GET /v1/banks?country={}", country);
        List<BankDto> dtos = service.list(country);
        List<BankResponse> out = dtos.stream()
                .map(mapper::toResponse)
                .toList();
        log.info("List banks -> {} results (country={})", out.size(), country);
        return ResponseEntity.ok(out);
    }

    @Operation(summary = "Update a bank (full replace). Requires If-Match with expected version")
    @PutMapping("/{id}")
    public ResponseEntity<BankResponse> update(
            @PathVariable UUID id,
            @RequestHeader(name = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody BankRequest req) {

        if (ifMatch == null || ifMatch.isBlank()) {
            log.warn("PUT /v1/banks/{} missing If-Match header", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.WARNING, "Missing If-Match header for optimistic locking")
                    .build();
        }
        long expectedVersion;
        try {
            expectedVersion = Long.parseLong(ifMatch.replace("\"", "").trim());
        } catch (NumberFormatException ex) {
            log.warn("PUT /v1/banks/{} invalid If-Match: {}", id, ifMatch);
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.WARNING, "If-Match must be a numeric version")
                    .build();
        }

        log.info("PUT /v1/banks/{} expectedVersion={} name='{}' bic='{}'",
                id, expectedVersion, req.name(), req.bic());

        BankDto input = mapper.toDto(req);
        BankDto updated = service.update(id, expectedVersion, input);

        BankResponse response = mapper.toResponse(updated);

        log.info("Bank updated id={} newVersion={}", id, response.version());
        return ResponseEntity.ok()
                .eTag("\"" + response.version() + "\"")
                .body(response);
    }

    @Operation(summary = "Delete a bank by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("DELETE /v1/banks/{}", id);
        service.delete(id);
        log.info("Bank deleted id={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Proxy that self-calls GET /v1/banks")
    @GetMapping("/proxy")
    public ResponseEntity<String> proxy(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) String country) {
        String uri = (country == null || country.isBlank())
                ? "/v1/banks"
                : "/v1/banks?country=" + country;

        log.info("GET /v1/banks/proxy -> forwarding to {}", uri);

        String body = webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.debug("Proxy response received ({} chars)", body != null ? body.length() : 0);
        return ResponseEntity.ok(body);
    }
}
