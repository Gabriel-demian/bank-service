package com.example.bankservice.application.controller;

import com.example.bankservice.application.dto.BankApiMapper;
import com.example.bankservice.application.dto.BankRequest;
import com.example.bankservice.application.dto.BankResponse;
import com.example.bankservice.domain.model.Bank;
import com.example.bankservice.domain.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/v1/banks")
@Tag(name = "Banks", description = "CRUD for bank entities + self-call proxy")
public class BankController {

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
        Bank created = service.create(req.name(), req.bic(), req.country(), req.routingNumber());
        URI location = URI.create("/v1/banks/" + created.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }

    @Operation(summary = "Get a bank by id")
    @GetMapping("/{id}")
    public ResponseEntity<BankResponse> get(@PathVariable UUID id) {
        Bank bank = service.get(id);
        return ResponseEntity.ok(mapper.toResponse(bank));
    }

    @Operation(summary = "List banks (optional in-memory filter by country)")
    @GetMapping
    public ResponseEntity<List<BankResponse>> list(@RequestParam(required = false) String country) {
        List<BankResponse> out = service.list().stream()
                .filter(b -> country == null || country.equalsIgnoreCase(b.getCountry()))
                .map(mapper::toResponse)
                .collect(toList());
        return ResponseEntity.ok(out);
    }

    @Operation(summary = "Update a bank (full replace). Requires If-Match with expected version")
    @PutMapping("/{id}")
    public ResponseEntity<BankResponse> update(
            @PathVariable UUID id,
            @RequestHeader(name = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody BankRequest req) {

        if (ifMatch == null || ifMatch.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.WARNING, "Missing If-Match header for optimistic locking")
                    .build();
        }
        long expectedVersion;
        try {
            expectedVersion = Long.parseLong(ifMatch.replace("\"", "").trim());
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.WARNING, "If-Match must be a numeric version")
                    .build();
        }

        Bank updated = service.update(
                id, req.name(), req.bic(), req.country(), req.routingNumber(), expectedVersion);

        return ResponseEntity.ok()
                .eTag("\"" + updated.getVersion() + "\"")
                .body(mapper.toResponse(updated));
    }

    @Operation(summary = "Delete a bank by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Proxy that self-calls GET /v1/banks (demo of API composition)")
    @GetMapping("/proxy")
    public ResponseEntity<String> proxy(@RequestParam(required = false) String country) {
        String uri = (country == null || country.isBlank())
                ? "/v1/banks"
                : "/v1/banks?country=" + country;

        String body = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(body);
    }
}
