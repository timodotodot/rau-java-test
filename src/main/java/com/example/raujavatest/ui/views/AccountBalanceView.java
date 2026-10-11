package com.example.raujavatest.ui.views;

import com.example.raujavatest.domain.registers.AccountBalance;
import org.springframework.stereotype.Component;
import su.onno.ui.EntityConfigBuilder;
import su.onno.ui.EntityView;
import su.onno.ui.ListSpec;

@Component
public class AccountBalanceView implements EntityView<AccountBalance> {

    @Override
    public Class<AccountBalance> entity() {
        return AccountBalance.class;
    }

    @Override
    public void list(ListSpec<AccountBalance> list) {
        list.columns(
                        AccountBalance::getCustomer,
                        AccountBalance::getAmount
                )
                .label(AccountBalance::getCustomer, "Клиент")
                .label(AccountBalance::getAmount, "Остаток")
                .sortBy(AccountBalance::getCustomer, false);
    }

    @Override
    public void fields(EntityConfigBuilder<AccountBalance> fields) {
        fields.field(AccountBalance::getCustomer)
                .label("Клиент");

        fields.field(AccountBalance::getAmount)
                .label("Остаток на лицевом счёте")
                .format("currency:RUB")
                .hint("Платежи увеличивают остаток, проведённые подписки уменьшают его");
    }
}