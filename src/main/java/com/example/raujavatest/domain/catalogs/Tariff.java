package com.example.raujavatest.domain.catalogs;

import lombok.Getter;
import lombok.Setter;
import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Catalog;
import su.onno.model.CatalogObject;

import java.math.BigDecimal;

@Catalog(name = "Tariffs", title = "Тарифы", codePrefix = "TR-", context = "Subscriptions")
@AccessControl(readRoles = {"MANAGER"}, writeRoles = {"MANAGER"})
@Getter
@Setter
public class Tariff extends CatalogObject {
    @Attribute(displayName = "Цена за период", required = true, precision = 15, scale = 2, min = 0)
    private BigDecimal pricePerPeriod = BigDecimal.ZERO;

    @Attribute(displayName = "Длительность периода, дней", required = true, min = 1)
    private Integer periodDurationDays = 30;

    @Attribute(displayName = "Доступен для подключения", required = true)
    private Boolean available = true;
}