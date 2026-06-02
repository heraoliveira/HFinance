package com.hfinance.ui.viewmodel;

import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.core.format.MoneyFormatter;

public record BudgetViewModel(
        Long id,
        String name,
        String category,
        String period,
        String limit,
        String spent,
        String usage,
        String status
) {
    public static BudgetViewModel from(BudgetDTO budget) {
        return new BudgetViewModel(budget.id(), budget.name(), budget.categoryName(),
                "%02d/%d".formatted(budget.month(), budget.year()), MoneyFormatter.format(budget.limitAmount()),
                MoneyFormatter.format(budget.spentAmount()), budget.usagePercent() + "%", budget.statusLabel());
    }
}
