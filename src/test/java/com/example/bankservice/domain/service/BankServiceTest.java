package com.example.bankservice.domain.service;

import com.example.bankservice.domain.model.Bank;
import com.example.bankservice.domain.port.BankRepositoryPort;
import com.example.bankservice.shared.exception.DuplicateResourceException;
import com.example.bankservice.shared.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    BankRepositoryPort repository;

    @InjectMocks
    BankService service;

    private static Bank newBank(String name, String bic, String country, String rn) {
        return Bank.newBank(name, bic, country, rn);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        void creates_when_valid_and_unique() {
            // given
            String name = "Banco Patagonia";
            String bic = "PATAGONIA01";
            String country = "AR";
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

            // when
            Bank out = service.create(name, bic, country, rn);

            // then
            then(repository).should().save(saved.capture());
            assertThat(saved.getValue().getName()).isEqualTo(name);
            assertThat(saved.getValue().getBic()).isEqualTo(bic);
            assertThat(saved.getValue().getCountry()).isEqualTo(country);
            assertThat(saved.getValue().getRoutingNumber()).isEqualTo(rn);

            assertThat(out.getId()).isNotNull();
            assertThat(out.getVersion()).isEqualTo(0L);
        }

        @Test
        void fails_when_duplicate_bic() {
            String name = "X";
            String bic = "DUPLBIC01AB";
            String country = "AR";

            given(repository.findByBic(bic)).willReturn(Optional.of(newBank("Y", bic, country, "r")));

            assertThatThrownBy(() -> service.create(name, bic, country, "r"))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("BIC");
        }

        @Test
        void fails_when_duplicate_name_country() {
            String name = "Banco Patagonia";
            String bic = "UNIQUEBIC12";
            String country = "AR";

            given(repository.findByBic(bic)).willReturn(Optional.empty());
            given(repository.findByNameAndCountry(name, country)).willReturn(Optional.of(newBank(name, "OTHERBIC12", country, "r")));

            assertThatThrownBy(() -> service.create(name, bic, country, "r"))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("name")
                    .hasMessageContaining("country");
        }

        @Test
        void fails_when_invalid_bic() {
            assertThatThrownBy(() -> service.create("A", "bad-bic", "AR", "r"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("BIC");
        }

        @Test
        void fails_when_invalid_country() {
            assertThatThrownBy(() -> service.create("A", "PATAGONIA01", "Ar", "r"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ISO-3166-1");
        }

        @Test
        void fails_when_required_fields_blank() {
            assertThatThrownBy(() -> service.create("", "PATAGONIA01", "AR", "r"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");

            assertThatThrownBy(() -> service.create("A", "", "AR", "r"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("bic");

            assertThatThrownBy(() -> service.create("A", "PATAGONIA01", "", "r"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("country");
        }
    }

    @Nested
    @DisplayName("read")
    class ReadTests {

        @Test
        void get_returns_entity() {
            UUID id = UUID.randomUUID();
            Bank b = newBank("A", "BICCODE01AB", "AR", "r");
            b.setId(id);

            given(repository.findById(id)).willReturn(Optional.of(b));

            Bank out = service.get(id);
            assertThat(out.getId()).isEqualTo(id);
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
                    newBank("A", "BICCODE01AB", "AR", "1"),
                    newBank("B", "BICCODE02AB", "US", "2")
            ));

            List<Bank> all = service.list();
            assertThat(all).hasSize(2);
        }
    }

    @Nested
    @DisplayName("update (full replace + optimistic check)")
    class UpdateTests {

        @Test
        void updates_when_version_matches_and_no_uniqueness_change() {
            UUID id = UUID.randomUUID();
            Bank existing = newBank("Banco", "PATAGONIA01", "AR", "111");
            existing.setId(id);
            existing.setVersion(3L);

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.save(any(Bank.class))).willAnswer(inv -> inv.getArgument(0));

            Bank out = service.update(id, "Banco S.A.", "PATAGONIA01", "AR", "999", 3);

            assertThat(out.getName()).isEqualTo("Banco S.A.");
            assertThat(out.getRoutingNumber()).isEqualTo("999");
            assertThat(out.getVersion()).isEqualTo(3L);
        }

        @Test
        void updates_when_changing_bic_to_unique() {
            UUID id = UUID.randomUUID();
            Bank existing = newBank("Banco", "PATAGONIA01", "AR", "111");
            existing.setId(id);
            existing.setVersion(0L);

            String newBic = "PATAGONIA02";

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.findByBic(newBic)).willReturn(Optional.empty());
            given(repository.save(any(Bank.class))).willAnswer(inv -> inv.getArgument(0));

            Bank out = service.update(id, "Banco", newBic, "AR", "111", 0);

            assertThat(out.getBic()).isEqualTo(newBic);
        }

        @Test
        void fails_when_version_conflict() {
            UUID id = UUID.randomUUID();
            Bank existing = newBank("Banco", "PATAGONIA01", "AR", "111");
            existing.setId(id);
            existing.setVersion(10L);

            given(repository.findById(id)).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.update(id, "Banco", "PATAGONIA01", "AR", "111", 9))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Version");
        }

        @Test
        void fails_when_changing_bic_to_existing_other() {
            UUID id = UUID.randomUUID();
            Bank existing = newBank("Banco", "PATAGONIA01", "AR", "111");
            existing.setId(id);
            existing.setVersion(0L);

            String duplicatedBic = "DUPLBIC01AB";
            Bank other = newBank("Otro", duplicatedBic, "AR", "r");
            other.setId(UUID.randomUUID());

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.findByBic(duplicatedBic)).willReturn(Optional.of(other));

            assertThatThrownBy(() -> service.update(id, "Banco", duplicatedBic, "AR", "111", 0))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("BIC");
        }

        @Test
        void fails_when_changing_name_country_to_existing_other() {
            UUID id = UUID.randomUUID();
            Bank existing = newBank("Banco", "PATAGONIA01", "AR", "111");
            existing.setId(id);
            existing.setVersion(0L);

            Bank other = newBank("Banco S.A.", "OTHERBIC01", "AR", "r");
            other.setId(UUID.randomUUID());

            given(repository.findById(id)).willReturn(Optional.of(existing));
            given(repository.findByNameAndCountry("Banco S.A.", "AR")).willReturn(Optional.of(other));

            assertThatThrownBy(() -> service.update(id, "Banco S.A.", "PATAGONIA01", "AR", "111", 0))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("name")
                    .hasMessageContaining("country");
        }

        @Test
        void fails_when_invalid_bic_or_country_or_required() {
            UUID id = UUID.randomUUID();

            // name requerido
            assertThatThrownBy(() -> service.update(id, "", "PATAGONIA01", "AR", "x", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");

            // BIC inválido
            assertThatThrownBy(() -> service.update(id, "Banco", "bad-bic", "AR", "x", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("BIC");

            // country inválido
            assertThatThrownBy(() -> service.update(id, "Banco", "PATAGONIA01", "Ar", "x", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ISO-3166-1");
        }

        @Test
        void fails_when_id_not_found() {
            UUID id = UUID.randomUUID();
            given(repository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(id, "Banco", "PATAGONIA01", "AR", "111", 0))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        void deletes_when_exists() {
            UUID id = UUID.randomUUID();
            Bank existing = newBank("A", "BICCODE01AB", "AR", "r");
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
