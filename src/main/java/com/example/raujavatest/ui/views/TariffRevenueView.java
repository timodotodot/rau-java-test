package com.example.raujavatest.ui.views;

import com.example.raujavatest.domain.registers.TariffRevenue;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class TariffRevenueView implements EntityView<TariffRevenue> {

    @Override
    public Class<TariffRevenue> entity() {
        return TariffRevenue.class;
    }

    @Override
    public void list(ListSpec<TariffRevenue> list) {
        list.columns(
                        TariffRevenue::getPeriod,
                        TariffRevenue::getTariff,
                        TariffRevenue::getCustomer,
                        TariffRevenue::getPeriods,
                        TariffRevenue::getAmount
                )
                .label(TariffRevenue::getPeriod, "Период")
                .label(TariffRevenue::getTariff, "Тариф")
                .label(TariffRevenue::getCustomer, "Клиент")
                .label(TariffRevenue::getPeriods, "Количество периодов")
                .label(TariffRevenue::getAmount, "Выручка")
                .sortBy(TariffRevenue::getPeriod, true);

        list.filter(TariffRevenue::getPeriod)
                .label("Период")
                .dateRange();
    }

    @Override
    public void fields(EntityConfigBuilder<TariffRevenue> fields) {
        fields.field(TariffRevenue::getPeriod)
                .label("Период")
                .format("dd.MM.yyyy HH:mm");

        fields.field(TariffRevenue::getTariff)
                .label("Тариф");

        fields.field(TariffRevenue::getCustomer)
                .label("Клиент");

        fields.field(TariffRevenue::getPeriods)
                .label("Количество периодов")
                .format("integer");

        fields.field(TariffRevenue::getAmount)
                .label("Выручка")
                .format("currency:RUB");
    }
}