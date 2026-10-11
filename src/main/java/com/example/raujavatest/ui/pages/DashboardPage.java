package com.example.raujavatest.ui.pages;

import com.example.raujavatest.domain.catalogs.Customer;
import com.example.raujavatest.domain.documents.Payment;
import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.domain.enumerations.SubscriptionStatus;
import com.example.raujavatest.domain.registers.AccountBalance;
import com.example.raujavatest.domain.registers.TariffRevenue;
import org.springframework.stereotype.Component;
import su.onno.repository.EnumerationPersistence;
import su.onno.ui.Page;
import su.onno.ui.PageBuilder;

@Component
public class DashboardPage implements Page {

    @Override
    public String route() {
        return "/";
    }

    @Override
    public void compose(PageBuilder page) {
        page.title("Дашборд")
                .subtitle("Основные показатели сервиса управления подписками");

        page.widget("Общий баланс клиентов")
                .type("metric")
                .register(AccountBalance.class)
                .config("metric", "sum")
                .metricField(AccountBalance::getAmount)
                .config("currency", "RUB")
                .config("locale", "ru-RU")
                .width("1/4")
                .hint("Суммарный остаток на лицевых счетах всех клиентов");

        page.widget("Общая выручка")
                .type("metric")
                .register(TariffRevenue.class)
                .config("metric", "sum")
                .metricField(TariffRevenue::getAmount)
                .config("currency", "RUB")
                .config("locale", "ru-RU")
                .width("1/4")
                .hint("Сумма проведённых подписок");

        page.widget("Активные подписки")
                .type("count")
                .document(Subscription.class)
                .config("filter", activeSubscriptionFilter())
                .config("locale", "ru-RU")
                .width("1/4");

        page.widget("Клиенты")
                .type("count")
                .catalog(Customer.class)
                .config("locale", "ru-RU")
                .width("1/4");

        page.widget("Выручка по тарифам")
                .type("chart")
                .register(TariffRevenue.class)
                .config("metric", "sum")
                .metricField(TariffRevenue::getAmount)
                .config("groupBy", "tariff_display")
                .config("kind", "bar")
                .config("currency", "RUB")
                .config("locale", "ru-RU")
                .width("2/3")
                .rowBreak();

        page.widget("Последние платежи")
                .type("list")
                .document(Payment.class)
                .dateField(Payment::getDate)
                .titleField(Payment::getNumber)
                .maxItems(5)
                .width("1/3");
    }

    private String activeSubscriptionFilter() {
        return "status = " + EnumerationPersistence.resolveId(
                SubscriptionStatus.class,
                SubscriptionStatus.ACTIVE
        );
    }
}
