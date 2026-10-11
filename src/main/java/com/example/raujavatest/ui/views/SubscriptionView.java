package com.example.raujavatest.ui.views;

import com.example.raujavatest.domain.catalogs.Tariff;
import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.domain.documents.SubscriptionLine;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class SubscriptionView implements EntityView<Subscription> {

    @Override
    public Class<Subscription> entity() {
        return Subscription.class;
    }

    @Override
    public void list(ListSpec<Subscription> list) {
        list.columns(
                        Subscription::getNumber,
                        Subscription::getDate,
                        Subscription::getCustomer,
                        Subscription::getStatus,
                        Subscription::getStartDate,
                        Subscription::getEndDate,
                        Subscription::getTotal,
                        Subscription::isPosted
                )
                .label(Subscription::getNumber, "Номер")
                .label(Subscription::getDate, "Дата документа")
                .label(Subscription::getCustomer, "Клиент")
                .label(Subscription::getStatus, "Статус")
                .label(Subscription::getStartDate, "Дата начала")
                .label(Subscription::getEndDate, "Дата окончания")
                .label(Subscription::getTotal, "Итого")
                .label(Subscription::isPosted, "Проведена")
                .sortBy(Subscription::getDate, true);

        list.filter(Subscription::getStatus)
                .label("Статус")
                .multiOptions();

        list.filter(Subscription::getDate)
                .label("Дата документа")
                .dateRange();

        list.filter(Subscription::getStartDate)
                .label("Дата начала")
                .dateRange();
    }

    @Override
    public void fields(EntityConfigBuilder<Subscription> fields) {
        fields.field(Subscription::getNumber)
                .label("Номер")
                .hideInForm();

        fields.field(Subscription::getDate)
                .label("Дата документа")
                .order(0)
                .width("half")
                .format("dd.MM.yyyy HH:mm")
                .hint("Автоматически заполняется при создании");

        fields.refField(Subscription::getCustomer)
                .label("Клиент")
                .order(1)
                .width("half")
                .hint("Стоимость подписки будет списана с лицевого счёта клиента");

        fields.field(Subscription::getStatus)
                .label("Статус")
                .order(2)
                .width("half");

        fields.field(Subscription::getStartDate)
                .label("Дата начала")
                .order(3)
                .width("half")
                .format("dd.MM.yyyy")
                .hint("По умолчанию устанавливается текущая дата");

        fields.field(Subscription::getEndDate)
                .label("Дата окончания")
                .order(4)
                .width("half")
                .format("dd.MM.yyyy")
                .hint("Рассчитывается автоматически по максимальному сроку среди строк")
                .hideInForm();

        fields.field(Subscription::getTotal)
                .label("Итого")
                .order(5)
                .width("half")
                .format("currency:RUB")
                .hint("Рассчитывается автоматически по строкам подписки")
                .hideInForm();

        fields.field(Subscription::isPosted)
                .label("Проведена")
                .hideInForm();

        fields.rowRefField(
                        Subscription::getLines,
                        SubscriptionLine::getTariff
                )
                .label("Тариф")
                .refSecondary(Tariff::getPricePerPeriod)
                .refFilter("available = true")
                .hint("Можно выбрать только существующий тариф");

        fields.rowField(
                        Subscription::getLines,
                        SubscriptionLine::getPeriods
                )
                .label("Число периодов")
                .format("integer")
                .hint("Должно быть больше нуля");

        fields.rowField(
                        Subscription::getLines,
                        SubscriptionLine::getPrice
                )
                .label("Цена")
                .format("currency:RUB")
                .hint("Автоматически подставляется из тарифа")
                .hideInForm();

        fields.rowField(
                        Subscription::getLines,
                        SubscriptionLine::getAmount
                )
                .label("Сумма")
                .format("currency:RUB")
                .hint("Цена тарифа, умноженная на число периодов")
                .hideInForm();
    }
}
