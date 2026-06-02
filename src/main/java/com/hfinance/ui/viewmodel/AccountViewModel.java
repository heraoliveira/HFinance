package com.hfinance.ui.viewmodel;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.core.format.MoneyFormatter;

public record AccountViewModel(
        Long id,
        String name,
        String institution,
        String type,
        String initialBalance,
        String currentBalance,
        String status
) {
    public static AccountViewModel from(AccountDTO account) {
        return new AccountViewModel(account.id(), account.name(), account.institution(), account.typeLabel(),
                MoneyFormatter.format(account.initialBalance()), MoneyFormatter.format(account.currentBalance()),
                account.statusLabel());
    }
}
