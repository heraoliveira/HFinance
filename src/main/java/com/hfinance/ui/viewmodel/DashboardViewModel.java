package com.hfinance.ui.viewmodel;

import com.hfinance.application.dto.DashboardSummaryDTO;
import com.hfinance.core.format.MoneyFormatter;

public record DashboardViewModel(
        String totalBalance,
        String currentMonthIncome,
        String currentMonthExpense,
        String currentMonthBalance
) {
    public static DashboardViewModel from(DashboardSummaryDTO dashboard) {
        return new DashboardViewModel(
                MoneyFormatter.format(dashboard.totalBalance()),
                MoneyFormatter.format(dashboard.currentMonthIncome()),
                MoneyFormatter.format(dashboard.currentMonthExpense()),
                MoneyFormatter.format(dashboard.currentMonthBalance())
        );
    }
}
