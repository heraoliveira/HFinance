package com.hfinance.ui.viewmodel;

import com.hfinance.application.dto.GoalDTO;
import com.hfinance.core.format.DateFormatter;
import com.hfinance.core.format.MoneyFormatter;

public record GoalViewModel(
        Long id,
        String name,
        String account,
        String targetAmount,
        String currentAmount,
        String deadline,
        String progress,
        String status
) {
    public static GoalViewModel from(GoalDTO goal) {
        return new GoalViewModel(goal.id(), goal.name(), goal.accountName(),
                MoneyFormatter.format(goal.targetAmount()), MoneyFormatter.format(goal.currentAmount()),
                DateFormatter.format(goal.deadline()), goal.progressPercent() + "%", goal.statusLabel());
    }
}
