package com.example.raujavatest.domain.catalogs;

import org.junit.jupiter.api.Test;
import su.onno.rules.BusinessRuleValidator;
import su.onno.validation.ValidationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void fillsMissingRegistrationDateWithToday() {
        Customer customer = new Customer();
        customer.setRegistrationDate(null);

        customer.onFilling();

        assertThat(customer.getRegistrationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void rejectsRegistrationDateInTheFuture() {
        Customer customer = new Customer();
        customer.setRegistrationDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> new BusinessRuleValidator().validate(customer))
                .isInstanceOf(ValidationException.class)
                .satisfies(exception -> {
                    ValidationException validationException = (ValidationException) exception;

                    assertThat(validationException.fieldErrors())
                            .containsKey("registrationDate");
                    assertThat(validationException.fieldErrors().get("registrationDate"))
                            .contains("Дата регистрации не может быть в будущем");
                });
    }

    @Test
    void acceptsRegistrationDateToday() {
        Customer customer = new Customer();
        customer.setRegistrationDate(LocalDate.now());

        new BusinessRuleValidator().validate(customer);
    }
}
