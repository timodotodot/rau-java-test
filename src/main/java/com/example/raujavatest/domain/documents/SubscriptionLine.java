package com.example.raujavatest.domain.documents;

import com.example.raujavatest.domain.catalogs.Tariff;
import lombok.Getter;
import lombok.Setter;
import su.onno.annotations.Attribute;
import su.onno.model.TabularSectionRow;
import su.onno.types.Ref;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionLine extends TabularSectionRow {
    @Attribute(displayName = "Тариф", required = true)
    private Ref<Tariff> tariff;

    @Attribute(displayName = "Число периодов", required = true, min = 1)
    private Integer periods = 1;

    @Attribute(displayName = "Цена", required = true, precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Attribute(displayName = "Сумма", required = true, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;
}
