package com.example.raujavatest.ui.layouts;

import com.example.raujavatest.domain.catalogs.Customer;
import com.example.raujavatest.domain.catalogs.Tariff;
import com.example.raujavatest.domain.documents.Payment;
import com.example.raujavatest.domain.documents.Subscription;
import com.example.raujavatest.domain.registers.AccountBalance;
import com.example.raujavatest.domain.registers.TariffRevenue;
import org.springframework.stereotype.Component;
import su.onno.ui.Layout;
import su.onno.ui.LayoutSpec;
import su.onno.ui.NavStyle;

@Component
public class MainLayout implements Layout {

    @Override
    public void configure(LayoutSpec layout) {
        configureShell(layout);
        configureNavigation(layout);
    }

    private void configureShell(LayoutSpec layout) {
        layout.shell()
                .nav(NavStyle.SIDEBAR)
                .brand("Управление подписками")
                .light(colors -> colors
                        .primary("#2563EB")
                        .primarySoft("#DBEAFE"))
                .dark(colors -> colors
                        .primary("#60A5FA")
                        .primarySoft("#1E3A5F"));
    }

    private void configureNavigation(LayoutSpec layout) {
        layout.section("Главное")
                .order(-1)
                .icon("layout-dashboard")
                .page("/", "Дашборд", "layout-dashboard");

        layout.section("Клиенты и тарифы")
                .order(0)
                .icon("users")
                .catalog(Customer.class)
                .catalog(Tariff.class);

        layout.section("Операции")
                .order(1)
                .icon("credit-card")
                .document(Payment.class)
                .document(Subscription.class);

        layout.section("Отчёты")
                .order(2)
                .icon("chart-column")
                .register(AccountBalance.class)
                .register(TariffRevenue.class);
    }
}
