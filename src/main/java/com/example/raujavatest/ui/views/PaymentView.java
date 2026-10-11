package com.example.raujavatest.ui.views;

import com.example.raujavatest.domain.documents.Payment;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class PaymentView implements EntityView<Payment> {

    @Override
    public Class<Payment> entity() {
        return Payment.class;
    }

    @Override
    public void list(ListSpec<Payment> list) {
        list.columns(
                        Payment::getNumber,
                        Payment::getDate,
                        Payment::getCustomer,
                        Payment::getAmount,
                        Payment::getPaymentMethod,
                        Payment::isPosted
                )
                .label(Payment::getNumber, "Номер")
                .label(Payment::getDate, "Дата")
                .label(Payment::getCustomer, "Клиент")
                .label(Payment::getAmount, "Сумма")
                .label(Payment::getPaymentMethod, "Способ оплаты")
                .label(Payment::isPosted, "Проведён")
                .sortBy(Payment::getDate, true);

        list.filter(Payment::getDate)
                .label("Дата платежа")
                .dateRange();

        list.filter(Payment::getPaymentMethod)
                .label("Способ оплаты")
                .multiOptions();
    }

    @Override
    public void fields(EntityConfigBuilder<Payment> fields) {
        fields.field(Payment::getNumber)
                .label("Номер")
                .hideInForm();

        fields.field(Payment::getDate)
                .label("Дата платежа")
                .order(0)
                .width("half")
                .format("dd.MM.yyyy HH:mm")
                .hint("Автоматически заполняется при создании");

        fields.refField(Payment::getCustomer)
                .label("Клиент")
                .order(1)
                .width("half")
                .hint("Лицевой счёт выбранного клиента будет пополнен");

        fields.field(Payment::getAmount)
                .label("Сумма")
                .order(2)
                .width("half")
                .format("currency:RUB")
                .hint("Сумма должна быть больше нуля");

        fields.field(Payment::getPaymentMethod)
                .label("Способ оплаты")
                .order(3)
                .width("half");

        fields.field(Payment::isPosted)
                .label("Проведён")
                .hideInForm();
    }
}