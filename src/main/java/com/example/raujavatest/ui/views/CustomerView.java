package com.example.raujavatest.ui.views;

import com.example.raujavatest.domain.catalogs.Customer;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class CustomerView implements EntityView<Customer> {

    @Override
    public Class<Customer> entity() {
        return Customer.class;
    }

    @Override
    public void list(ListSpec<Customer> list) {
        list.columns(
                Customer::getCode,
                Customer::getDescription,
                Customer::getStatus,
                Customer::getEmail,
                Customer::getPhone,
                Customer::getRegistrationDate
        )
                .label(Customer::getCode, "Код")
                .label(Customer::getDescription, "Имя")
                .label(Customer::getStatus, "Статус")
                .label(Customer::getEmail, "E-mail")
                .label(Customer::getPhone, "Телефон")
                .label(Customer::getRegistrationDate, "Дата регистрации")
                .sortBy(Customer::getDescription, false);

        list.filter(Customer::getStatus)
                .label("Статус")
                .multiOptions();

        list.filter(Customer::getEmail)
                .label("E-mail")
                .contains();
    }

    public void fields(EntityConfigBuilder<Customer> fields) {
        fields.field(Customer::getCode)
                .label("Код")
                .hideInForm()

                .field(Customer::getDescription)
                .label("Имя клиента")
                .order(0)
                .width("full")
                .placeholder("Например, Иван Иванов")
                .hint("Имя клиента или название организации")

                .field(Customer::getStatus)
                .label("Статус")
                .order(1)
                .width("half")
                .hint("Заблокированный клиент не должен получать новые подписки")

                .field(Customer::getRegistrationDate)
                .label("Дата регистрации")
                .order(2)
                .width("half")
                .hideInForm()
                .format("dd.MM.yyyy")
                .hint("Автоматически заполняется при создании")

                .field(Customer::getEmail)
                .label("E-mail")
                .order(3)
                .width("half")
                .placeholder("client@example.com")

                .field(Customer::getPhone)
                .label("Телефон")
                .order(4)
                .width("half")
                .placeholder("+7 999 123-45-67");
    }
}
