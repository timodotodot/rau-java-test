package com.example.raujavatest.domain.documents;

import com.example.raujavatest.domain.catalogs.Customer;
import com.example.raujavatest.domain.enumerations.PaymentMethod;
import com.example.raujavatest.domain.registers.AccountBalance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import su.onno.posting.PostingContext;
import su.onno.repository.RegisterRepository;
import su.onno.rules.BusinessRuleValidator;
import su.onno.types.Ref;
import su.onno.validation.ValidationException;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentTest {

    @Test
    void rejectsPaymentWithoutCustomerAndPositiveAmount() {
        Payment payment = new Payment();
        BusinessRuleValidator validator = new BusinessRuleValidator();

        assertThatThrownBy(() -> validator.validate(payment))
                .isInstanceOf(ValidationException.class)
                .satisfies(exception -> {
                    ValidationException validationException =
                            (ValidationException) exception;

                    assertThat(validationException.fieldErrors())
                            .containsKeys("customer", "amount");
                });
    }

    @Test
    void createsAccountBalanceReceiptDuringPosting() {
        Ref<Customer> customer =
                Ref.of(Customer.class, UUID.randomUUID());

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setAmount(new BigDecimal("1500.00"));
        payment.setPaymentMethod(PaymentMethod.CARD);

        PostingContext context = mock(PostingContext.class);

        @SuppressWarnings("unchecked")
        RegisterRepository<AccountBalance> accountBalance =
                mock(RegisterRepository.class);

        when(context.movements(AccountBalance.class))
                .thenReturn(accountBalance);

        payment.handlePosting(context);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<AccountBalance>> movementCaptor =
                ArgumentCaptor.forClass(Consumer.class);

        verify(accountBalance)
                .addReceipt(movementCaptor.capture());

        AccountBalance movement = new AccountBalance();
        movementCaptor.getValue().accept(movement);

        assertThat(movement.getCustomer())
                .isEqualTo(customer);

        assertThat(movement.getAmount())
                .isEqualByComparingTo("1500.00");
    }
}