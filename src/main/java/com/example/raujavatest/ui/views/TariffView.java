package com.example.raujavatest.ui.views;

import com.example.raujavatest.domain.catalogs.Tariff;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class TariffView implements EntityView<Tariff> {

    @Override
    public Class<Tariff> entity() {
        return Tariff.class;
    }

    @Override
    public void list(ListSpec<Tariff> list) {
        list.columns(
                        Tariff::getCode,
                        Tariff::getDescription,
                        Tariff::getPricePerPeriod,
                        Tariff::getPeriodDurationDays,
                        Tariff::getAvailable
                )
                .label(Tariff::getCode, "Код")
                .label(Tariff::getDescription, "Название")
                .label(Tariff::getPricePerPeriod, "Цена за период")
                .label(Tariff::getPeriodDurationDays, "Дней в периоде")
                .label(Tariff::getAvailable, "Доступен")
                .sortBy(Tariff::getDescription, false);
    }

    @Override
    public void fields(EntityConfigBuilder<Tariff> fields) {
        fields.field(Tariff::getCode)
                .label("Код")
                .hideInForm()

                .field(Tariff::getDescription)
                .label("Название тарифа")
                .order(0)
                .width("full")
                .placeholder("Например, Премиум")
                .hint("Название, которое пользователь увидит при выборе тарифа")

                .field(Tariff::getPricePerPeriod)
                .label("Цена за период")
                .order(1)
                .width("half")
                .format("currency:RUB")
                .hint("Стоимость одного периода действия тарифа")

                .field(Tariff::getPeriodDurationDays)
                .label("Длительность периода")
                .order(2)
                .width("half")
                .format("integer")
                .hint("Продолжительность одного периода в днях")

                .field(Tariff::getAvailable)
                .label("Доступен для подключения")
                .order(3)
                .widget("switch")
                .hint("Недоступный тариф нельзя добавить в новую подписку");
    }
}