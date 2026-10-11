package com.example.raujavatest.domain.catalogs;

import com.example.raujavatest.domain.enumerations.CustomerStatus;
import lombok.Getter;
import lombok.Setter;
import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Catalog;
import su.onno.lifecycle.OnFillingHandler;
import su.onno.model.CatalogObject;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;

import java.time.LocalDate;
import java.util.List;

@Catalog(name = "Customers", title = "Клиенты", codePrefix = "CL-", context = "Subscriptions")
@AccessControl(readRoles = {"MANAGER"}, writeRoles = {"MANAGER"})
@Getter
@Setter
public class Customer extends CatalogObject implements OnFillingHandler, Validated {
    @Attribute(displayName = "Статус", required = true)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Attribute(displayName = "E-mail", length = 200, email = true)
    private String email;

    @Attribute(displayName = "Телефон", length = 50)
    private String phone;

    @Attribute(displayName = "Дата регистрации")
    private LocalDate registrationDate;

    @Override
    public void onFilling() {
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                BusinessRule.onField(
                        "registrationDate",
                        "Дата регистрации обязательна",
                        () -> registrationDate != null
                ),
                BusinessRule.onField(
                        "registrationDate",
                        "Дата регистрации не может быть в будущем",
                        () -> registrationDate == null || !registrationDate.isAfter(LocalDate.now())
                )
        );
    }
}
