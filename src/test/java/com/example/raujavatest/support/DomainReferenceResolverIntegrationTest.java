package com.example.raujavatest.support;

import com.example.raujavatest.domain.catalogs.Tariff;
import com.example.raujavatest.domain.catalogs.Customer;
import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.domain.documents.SubscriptionLine;
import com.example.raujavatest.domain.enumerations.SubscriptionStatus;
import com.example.raujavatest.repositories.TariffRepository;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import su.onno.metadata.MetadataRegistry;
import su.onno.rules.BusinessRuleValidator;
import su.onno.types.Ref;
import su.onno.ui.DocumentCommandService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:reference_resolver_test")
class DomainReferenceResolverIntegrationTest {

    @Autowired
    private Jdbi jdbi;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private DocumentCommandService documentCommandService;

    @Autowired
    private MetadataRegistry metadataRegistry;

    @Test
    void resolvesTariffCreatedThroughTheUiTableAndAcceptsItInSubscription() {
        UUID tariffId = UUID.randomUUID();

        jdbi.useHandle(handle -> handle.createUpdate("""
                        INSERT INTO catalog_tariffs
                            (_id, _code, _description, _deletion_mark, _is_folder, _version,
                             price_per_period, period_duration_days, available)
                        VALUES
                            (:id, 'TR-TEST', 'Тестовый тариф', false, false, 0, 500.00, 30, true)
                        """)
                .bind("id", tariffId)
                .execute());

        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow();

        Tariff resolvedTariff = DomainReferenceResolver.resolve(Ref.of(Tariff.class, tariffId))
                .orElseThrow();

        assertThat(tariff.getPricePerPeriod()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(tariff.getAvailable()).isTrue();
        assertThat(resolvedTariff.getAvailable()).isTrue();

        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(Ref.of(Tariff.class, tariffId));
        line.setPeriods(1);

        Subscription subscription = new Subscription();
        subscription.setCustomer(Ref.of(Customer.class, UUID.randomUUID()));
        subscription.getLines().add(line);
        subscription.beforeWrite();

        new BusinessRuleValidator().validate(subscription);
        assertThat(subscription.getTotal()).isEqualByComparingTo("500.00");

        assertThat(documentCommandService)
                .isInstanceOf(SubscriptionDocumentCommandService.class);

        Map<String, Object> normalized = ((SubscriptionDocumentCommandService) documentCommandService)
                .normalize(
                        metadataRegistry.getDocumentDescriptor(Subscription.class),
                        Map.of("lines", List.of(Map.of(
                                "tariff", tariffId.toString(),
                                "periods", 2,
                                "price", new BigDecimal("999.00"),
                                "amount", new BigDecimal("1998.00")
                        )))
                );

        @SuppressWarnings("unchecked")
        Map<String, Object> normalizedLine = ((List<Map<String, Object>>) normalized.get("lines")).getFirst();
        assertThat(normalizedLine.get("price")).isEqualTo(new BigDecimal("500.00"));
        assertThat(normalizedLine.get("amount")).isEqualTo(new BigDecimal("1000.00"));

        UUID draftStatusId = metadataRegistry.getEnumerationDescriptor(SubscriptionStatus.class)
                .values().stream()
                .filter(value -> value.name().equals(SubscriptionStatus.DRAFT.name()))
                .findFirst()
                .orElseThrow()
                .id();

        Map<String, Object> created = documentCommandService.create(
                metadataRegistry.getDocumentDescriptor(Subscription.class),
                Map.of(
                        "customer", UUID.randomUUID(),
                        "status", draftStatusId,
                        "startDate", LocalDate.of(2026, 10, 11),
                        "lines", List.of(Map.of(
                                "tariff", tariffId,
                                "periods", 2
                        ))
                ),
                UsernamePasswordAuthenticationToken.authenticated(
                        "test-manager",
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
                )
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> savedLine = ((List<Map<String, Object>>) created.get("lines")).getFirst();
        assertThat(savedLine.get("price")).isEqualTo(new BigDecimal("500.00"));
        assertThat(savedLine.get("amount")).isEqualTo(new BigDecimal("1000.00"));
        assertThat(created.get("total")).isEqualTo(new BigDecimal("1000.00"));
    }
}
