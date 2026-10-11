package com.example.raujavatest.domain.documents;

import com.example.raujavatest.domain.catalogs.Customer;
import com.example.raujavatest.domain.catalogs.Tariff;
import com.example.raujavatest.support.DomainReferenceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.onno.rules.BusinessRuleValidator;
import su.onno.types.Ref;
import su.onno.types.RefResolver;
import su.onno.validation.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    private final Map<UUID, Tariff> tariffs = new HashMap<>();

    @BeforeEach
    void configureReferenceResolver() {
        DomainReferenceResolver.useForTests(new RefResolver() {
            @Override
            public <T> Optional<T> resolve(Ref<T> reference) {
                Object entity = tariffs.get(reference.id());

                if (entity == null) {
                    return Optional.empty();
                }

                return Optional.of(reference.type().cast(entity));
            }
        });
    }

    @Test
    void calculatesLineAmountsTotalAndMaximumEndDate() {
        Tariff monthly = tariff(
                new BigDecimal("500.00"),
                30,
                true
        );

        Tariff annual = tariff(
                new BigDecimal("4000.00"),
                365,
                true
        );

        Subscription subscription = new Subscription();
        subscription.setStartDate(LocalDate.of(2026, 10, 1));
        subscription.getLines().add(line(monthly, 2));
        subscription.getLines().add(line(annual, 1));

        subscription.beforeWrite();

        assertThat(subscription.getLines().get(0).getPrice())
                .isEqualByComparingTo("500.00");

        assertThat(subscription.getLines().get(0).getAmount())
                .isEqualByComparingTo("1000.00");

        assertThat(subscription.getLines().get(1).getAmount())
                .isEqualByComparingTo("4000.00");

        assertThat(subscription.getTotal())
                .isEqualByComparingTo("5000.00");

        assertThat(subscription.getEndDate())
                .isEqualTo(LocalDate.of(2027, 10, 1));
    }

    @Test
    void rejectsSubscriptionWithoutCustomerAndLines() {
        Subscription subscription = new Subscription();
        BusinessRuleValidator validator = new BusinessRuleValidator();

        assertThatThrownBy(() -> validator.validate(subscription))
                .isInstanceOf(ValidationException.class)
                .satisfies(exception -> {
                    ValidationException validationException =
                            (ValidationException) exception;

                    assertThat(validationException.fieldErrors())
                            .containsKey("customer");

                    assertThat(validationException.formErrors())
                            .contains("Добавьте хотя бы одну строку подписки");
                });
    }

    @Test
    void rejectsUnavailableTariff() {
        Tariff unavailableTariff = tariff(
                new BigDecimal("1000.00"),
                30,
                false
        );

        Subscription subscription = new Subscription();
        subscription.setCustomer(
                Ref.of(Customer.class, UUID.randomUUID())
        );
        subscription.getLines().add(line(unavailableTariff, 1));
        subscription.beforeWrite();

        BusinessRuleValidator validator = new BusinessRuleValidator();

        assertThatThrownBy(() -> validator.validate(subscription))
                .isInstanceOf(ValidationException.class)
                .satisfies(exception -> {
                    ValidationException validationException =
                            (ValidationException) exception;

                    assertThat(validationException.formErrors())
                            .contains(
                                    "Все тарифы должны быть доступны для подключения"
                            );
                });
    }

    @Test
    void rejectsNonPositiveNumberOfPeriods() {
        Tariff tariff = tariff(
                new BigDecimal("500.00"),
                30,
                true
        );

        Subscription subscription = new Subscription();
        subscription.setCustomer(
                Ref.of(Customer.class, UUID.randomUUID())
        );
        subscription.getLines().add(line(tariff, 0));
        subscription.beforeWrite();

        BusinessRuleValidator validator = new BusinessRuleValidator();

        assertThatThrownBy(() -> validator.validate(subscription))
                .isInstanceOf(ValidationException.class)
                .satisfies(exception -> {
                    ValidationException validationException =
                            (ValidationException) exception;

                    assertThat(validationException.formErrors())
                            .contains(
                                    "Число периодов должно быть больше нуля"
                            );
                });
    }

    @Test
    void preservesHistoricalPriceForPostedSubscription() {
        Tariff tariff = tariff(
                new BigDecimal("900.00"),
                30,
                true
        );

        SubscriptionLine line = line(tariff, 2);
        line.setPrice(new BigDecimal("500.00"));

        Subscription subscription = new Subscription();
        subscription.setPosted(true);
        subscription.getLines().add(line);

        subscription.beforeWrite();

        assertThat(line.getPrice())
                .isEqualByComparingTo("500.00");

        assertThat(line.getAmount())
                .isEqualByComparingTo("1000.00");

        assertThat(subscription.getTotal())
                .isEqualByComparingTo("1000.00");
    }

    private Tariff tariff(
            BigDecimal price,
            int durationDays,
            boolean available
    ) {
        Tariff tariff = new Tariff();
        tariff.setId(UUID.randomUUID());
        tariff.setPricePerPeriod(price);
        tariff.setPeriodDurationDays(durationDays);
        tariff.setAvailable(available);

        tariffs.put(tariff.getId(), tariff);
        return tariff;
    }

    private SubscriptionLine line(
            Tariff tariff,
            int periods
    ) {
        SubscriptionLine line = new SubscriptionLine();
        line.setTariff(Ref.of(Tariff.class, tariff.getId()));
        line.setPeriods(periods);
        return line;
    }
}