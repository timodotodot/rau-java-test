package com.example.raujavatest.domain.documents;

import com.example.raujavatest.domain.catalogs.Customer;
import com.example.raujavatest.domain.catalogs.Tariff;
import com.example.raujavatest.domain.enumerations.SubscriptionStatus;
import com.example.raujavatest.domain.registers.AccountBalance;
import com.example.raujavatest.domain.registers.TariffRevenue;
import com.example.raujavatest.support.DomainReferenceResolver;
import lombok.Getter;
import lombok.Setter;
import su.onno.annotations.AccessControl;
import su.onno.annotations.Attribute;
import su.onno.annotations.Document;
import su.onno.annotations.TabularSection;
import su.onno.lifecycle.BeforeWriteHandler;
import su.onno.lifecycle.OnFillingHandler;
import su.onno.lifecycle.Postable;
import su.onno.model.DocumentObject;
import su.onno.posting.PostingContext;
import su.onno.rules.BusinessRule;
import su.onno.rules.Validated;
import su.onno.types.Ref;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(name = "Subscriptions", title = "Подписки", numberPrefix = "SUB-", context = "Subscriptions")
@AccessControl(readRoles = {"MANAGER"}, writeRoles = {"MANAGER"})
@Getter
@Setter
public class Subscription extends DocumentObject
        implements OnFillingHandler, BeforeWriteHandler, Validated, Postable {
    @Attribute(displayName = "Клиент", required = true)
    private Ref<Customer> customer;

    @Attribute(displayName = "Статус", required = true)
    private SubscriptionStatus status = SubscriptionStatus.DRAFT;

    @Attribute(displayName = "Дата начала", required = true)
    private LocalDate startDate = LocalDate.now();

    @Attribute(displayName = "Дата окончания")
    private LocalDate endDate;

    @Attribute(displayName = "Итого", precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @TabularSection(name = "lines")
    private List<SubscriptionLine> lines = new ArrayList<>();

    @Override
    public void onFilling() {
        if (getDate() == null) {
            setDate(LocalDateTime.now());
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (status == null) {
            status = SubscriptionStatus.DRAFT;
        }
    }

    @Override
    public void beforeWrite() {
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        long maximumDays = 0;

        if (lines == null) {
            lines = new ArrayList<>();
        }
        for (SubscriptionLine line : lines) {
            Tariff tariff = DomainReferenceResolver.resolve(line.getTariff()).orElse(null);
            if (tariff != null) {
                if (!isPosted()) {
                    line.setPrice(zeroIfNull(tariff.getPricePerPeriod()));
                }
                if (line.getPeriods() != null && tariff.getPeriodDurationDays() != null
                        && line.getPeriods() > 0 && tariff.getPeriodDurationDays() > 0) {
                    long lineDays = Math.multiplyExact((long) line.getPeriods(), tariff.getPeriodDurationDays());
                    maximumDays = Math.max(maximumDays, lineDays);
                }
            }
            BigDecimal lineAmount = zeroIfNull(line.getPrice())
                    .multiply(BigDecimal.valueOf(line.getPeriods() == null ? 0 : line.getPeriods()));
            line.setAmount(lineAmount);
            calculatedTotal = calculatedTotal.add(lineAmount);
        }
        total = calculatedTotal;
        endDate = startDate == null || maximumDays == 0 ? null : startDate.plusDays(maximumDays);
    }

    @Override
    public List<BusinessRule> rules() {
        return List.of(
                BusinessRule.onField("customer", "Выберите клиента", () -> customer != null),
                new BusinessRule("lines-required", "Добавьте хотя бы одну строку подписки",
                        () -> lines != null && !lines.isEmpty()),
                new BusinessRule("tariffs-required", "В каждой строке должен быть выбран тариф",
                        () -> lines != null && lines.stream().allMatch(line -> line.getTariff() != null)),
                new BusinessRule("periods-positive", "Число периодов должно быть больше нуля",
                        () -> lines != null && lines.stream().allMatch(line ->
                                line.getPeriods() != null && line.getPeriods() > 0)),
                new BusinessRule("tariffs-available", "Все тарифы должны быть доступны для подключения",
                        () -> lines != null && lines.stream().allMatch(this::isTariffAvailable)));
    }

    @Override
    public void handlePosting(PostingContext context) {
        if (status == SubscriptionStatus.CANCELLED) {
            return;
        }
        context.movements(AccountBalance.class).addExpense(movement -> {
            movement.setCustomer(customer);
            movement.setAmount(total);
        });

        var revenue = context.movements(TariffRevenue.class);
        for (SubscriptionLine line : lines) {
            revenue.addReceipt(movement -> {
                movement.setTariff(line.getTariff());
                movement.setCustomer(customer);
                movement.setAmount(line.getAmount());
                movement.setPeriods(BigDecimal.valueOf(line.getPeriods()));
            });
        }
    }

    private boolean isTariffAvailable(SubscriptionLine line) {
        return DomainReferenceResolver.resolve(line.getTariff())
                .map(Tariff::getAvailable)
                .orElse(false);
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
