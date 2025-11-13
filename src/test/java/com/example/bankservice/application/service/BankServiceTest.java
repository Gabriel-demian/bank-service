package com.example.bankservice.application.service;

import com.example.bankservice.application.dto.BankDto;
import com.example.bankservice.application.mapper.BankAppMapper;
import com.example.bankservice.domain.model.Bank;
import com.example.bankservice.domain.port.BankRepositoryPort;
import com.example.bankservice.shared.exception.DuplicateResourceException;
import com.example.bankservice.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    private static final String COUNTRY_AR = "AR";
    private static final String BIC_VALID_11 = "PATAGONIA01";
    private static final String BIC_VALID_11_ALT = "UNIQUEBIC12";
    private static final String BIC_DUP_11 = "DUPLBIC01AB";

    // Helpers ----------------------------------------------------

    private static BankDto dto(String name, String bic, String country, String rn) {
        return new BankDto(
                null, name, bic, country, rn, 0L, null, null
        );
    }

    private static Bank bankWith(String name, String bic, String country, String rn) {
        return Bank.newBank(name, bic, country, rn);
    }

    private static Bank bankWithIdAndVersion(UUID id, long version, String name, String bic, String country, String rn) {
        Bank b = bankWith(name, bic, country, rn);
        b.setId(id);
        b.setVersion(version);
        return b;
    }

    // Mocks ------------------------------------------------------

    @Mock
    BankRepositoryPort repository;

    @Mock
    BankAppMapper mapper;

    @InjectMocks
    BankService service;

    @BeforeEach
    void setUpMapper() {
        lenient().when(mapper.toDto(any(Bank.class))).thenAnswer(inv -> {
            Bank b = inv.getArgument(0);
            return new BankDto(
                    b.getId(),
                    b.getName(),
                    b.getBic(),
                    b.getCountry(),
                    b.getRoutingNumber(),
                    b.getVersion(),
                    b.getCreatedAt(),
                    b.getUpdatedAt()
            );
        });
    }

    // CREATE -----------------------------------------------------

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        void creates_when_valid_and_unique() {
            String name = "Banco Patagonia";
            String bic = BIC_VALID_11;
            String country = COUNTRY_AR;
            String rn = "123456";

            given(repository.findByBic(bic)).willReturn(Optional.empty());
            given(repository.findByNameAndCountry(name, country)).willReturn(Optional.empty());

            ArgumentCaptor<Bank> saved = ArgumentCaptor.forClass(Bank.class);

            given(repository.save(any(Bank.class))).willAnswer(inv -> {
                Bank in = inv.getArgument(0);
                in.setId(UUID.randomUUID());
                in.setVersion(0L);
                return in;
            });

            BankDto input = dto(name, bic, country, rn);

            BankDto out = service.create(input);

            then(repository).should().save(saved.capture());
            assertThat(saved.getValue().getName()).isEqualTo(name);
            assertThat(saved.getValue().getBic()).isEqualTo(bic);
            assertThat(saved.getValue().getCountry()).isEqualTo(country);
            assertThat(saved.getValue().getRoutingNumber()).isEqualTo(rn);

            assertThat(out.id()).isNotNull();
            assertThat(out.version()).isEqualTo(0L);
        }

        @Test
        void fails_when_duplicate_bic() {
            String name = "X";
            String bic = BIC_DUP_11;

            given(repository.findByBic(bic))
                    .willReturn(Optional.of(bankWith("Y", bic, COUNTRY_AR, "r")));

            assertThatThrownBy(() -> service.create(dto(name, bic, COUNTRY_AR, "r")))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("BIC");
        }

        @Test
        void fails_when_duplicate_name_country() {
            String name = "Banco Patagonia";
            String bic = BIC_VALID_11_ALT;

            given(repository.findByBic(bic)).willReturn(Optional.empty());
            given(repository.findByNameAndCountry(name, COUNTRY_AR))
                    .willReturn(Optional.of(bankWith(name, "OTHERBIC12", COUNTRY_AR, "r")));

            assertThatThrownBy(() -> service.create(dto(name, bic, COUNTRY_AR, "r")))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("name")
                    .hasMessageContaining("country");
        }

        static Stream<Object[]> requiredCreateCases() {
            return Stream.of(
                    new Object[]{"", BIC_VALID_11, COUNTRY_AR, "r", "name"},
                    new Object[]{"A", "", COUNTRY_AR, "r", "bic"},
                    new Object[]{"A", BIC_VALID_11, "", "r", "country"}
            );
        }

        @ParameterizedTest
        @MethodSource("requiredCreateCases")
        void create_fails_when_required_blank(String name, String bic, String country, String rn, String expectedField) {
            assertThatThrownBy(() -> service.create(dto(name, bic, country, rn)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedField);
            verifyNoInteractions(repository);
        }

        static Stream<Object[]> invalidFormatCreateCases() {
            return Stream.of(
                    new Object[]{"A", "bad-bic", COUNTRY_AR, "BIC"},
                    new Object[]{"A", BIC_VALID_11, "Ar", "ISO-3166-1"}
            );
        }

        @ParameterizedTest
        @MethodSource("invalidFormatCreateCases")
        void create_fails_when_invalid_format(String name, String bic, String country, String contains) {
            assertThatThrownBy(() -> service.create(dto(name, bic, country, "r")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(contains);
            verifyNoInteractions(repository);
        }
    }

    // READ -------------------------------------------------------

    @Nested
    @DisplayName("read")
    class ReadTests {

        @Test
        void get_returns_entity() {
            UUID id = UUID.randomUUID();
            Bank b = bankWith("A", "BICCODE01AB", COUNTRY_AR, "r");
            b.setId(id);

            given(repository.findById(id)).willReturn(Optional.of(b));

            BankDto out = service.get(id);
            assertThat(out.id()).isEqualTo(id);
        }

        @Test
        void get_throws_when_not_found() {
            UUID id = UUID.randomUUID();
            given(repository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.get(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        void list_returns_all() {
            given(repository.findAll()).willReturn(List.of(
                    bankWith("A", "BICCODE01AB", COUNTRY_AR, "1"),
                    bankWith("B", "BICCODE02AB", "US", "2")
            ));

            List<BankDto> all = service.list(null);
            assertThat(all).hasSize(2);
        }
    }

    // UPDATE -----------------------------------------------------

    @Nested
    @DisplayName("update (full replace + optimistic check)")
    class UpdateTests {

        @Test
        void updates_when_version_matches_and_no_uniqueness_change() {
            UUID id = UUID.randomUUID();
            Bank existing = bankWithIdAndVersion(id, 3L, "Banco", BIC_VALID_11, COUNTRY_AR, "111");

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.save(any(Bank.class))).willAnswer(inv -> inv.getArgument(0));

            BankDto input = dto("Banco S.A.", BIC_VALID_11, COUNTRY_AR, "999");

            BankDto out = service.update(id, 3, input);

            assertThat(out.name()).isEqualTo("Banco S.A.");
            assertThat(out.routingNumber()).isEqualTo("999");
        }

        @Test
        void updates_when_changing_bic_to_unique() {
            UUID id = UUID.randomUUID();
            Bank existing = bankWithIdAndVersion(id, 0L, "Banco", BIC_VALID_11, COUNTRY_AR, "111");
            String newBic = "PATAGONIA02";

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.findByBic(newBic)).willReturn(Optional.empty());
            given(repository.save(any(Bank.class))).willAnswer(inv -> inv.getArgument(0));

            BankDto input = dto("Banco", newBic, COUNTRY_AR, "111");

            BankDto out = service.update(id, 0, input);

            assertThat(out.bic()).isEqualTo(newBic);
        }

        @Test
        void fails_when_version_conflict() {
            UUID id = UUID.randomUUID();
            Bank existing = bankWithIdAndVersion(id, 10L, "Banco", BIC_VALID_11, COUNTRY_AR, "111");

            given(repository.findById(id)).willReturn(Optional.of(existing));

            assertThatThrownBy(() ->
                    service.update(id, 9, dto("Banco", BIC_VALID_11, COUNTRY_AR, "111")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Version");
        }

        @Test
        void fails_when_changing_bic_to_existing_other() {
            UUID id = UUID.randomUUID();
            Bank existing = bankWithIdAndVersion(id, 0L, "Banco", BIC_VALID_11, COUNTRY_AR, "111");

            String duplicatedBic = BIC_DUP_11;
            Bank other = bankWith("Otro", duplicatedBic, COUNTRY_AR, "r");
            other.setId(UUID.randomUUID());

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.findByBic(duplicatedBic)).willReturn(Optional.of(other));

            assertThatThrownBy(() ->
                    service.update(id, 0, dto("Banco", duplicatedBic, COUNTRY_AR, "111")))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("BIC");
        }

        @Test
        void fails_when_changing_name_country_to_existing_other() {
            UUID id = UUID.randomUUID();
            Bank existing = bankWithIdAndVersion(id, 0L, "Banco", BIC_VALID_11, COUNTRY_AR, "111");

            Bank other = bankWith("Banco S.A.", "OTHERBIC01", COUNTRY_AR, "r");
            other.setId(UUID.randomUUID());

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.findByNameAndCountry("Banco S.A.", COUNTRY_AR)).willReturn(Optional.of(other));

            assertThatThrownBy(() ->
                    service.update(id, 0, dto("Banco S.A.", BIC_VALID_11, COUNTRY_AR, "111")))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("name")
                    .hasMessageContaining("country");
        }

        static Stream<Object[]> invalidUpdateCases() {
            UUID any = UUID.randomUUID();
            return Stream.of(
                    new Object[]{any, "", BIC_VALID_11, COUNTRY_AR, "name"},
                    new Object[]{any, "Banco", "bad-bic", COUNTRY_AR, "BIC"},
                    new Object[]{any, "Banco", BIC_VALID_11, "Ar", "ISO-3166-1"}
            );
        }

        @ParameterizedTest
        @MethodSource("invalidUpdateCases")
        void update_fails_when_invalid_early(UUID id, String name, String bic, String country, String contains) {
            assertThatThrownBy(() ->
                    service.update(id, 0, dto(name, bic, country, "x")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(contains);
            verifyNoInteractions(repository);
        }

        @Test
        void fails_when_id_not_found() {
            UUID id = UUID.randomUUID();
            given(repository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.update(id, 0, dto("Banco", BIC_VALID_11, COUNTRY_AR, "111")))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // DELETE -----------------------------------------------------

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        void deletes_when_exists() {
            UUID id = UUID.randomUUID();
            Bank existing = bankWith("A", "BICCODE01AB", COUNTRY_AR, "r");
            existing.setId(id);

            given(repository.findById(id)).willReturn(Optional.of(existing));
            willDoNothing().given(repository).deleteById(id);

            service.delete(id);

            then(repository).should().deleteById(id);
        }

        @Test
        void fails_when_not_found() {
            UUID id = UUID.randomUUID();
            given(repository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }
}
