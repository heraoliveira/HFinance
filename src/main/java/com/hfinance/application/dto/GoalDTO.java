package com.hfinance.application.dto;

import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.domain.model.Goal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalDTO(
        Long id,
        Long accountId,
        String accountName,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        BigDecimal progressPercent,
        GoalStatus status,
        String statusLabel
) {
    public static GoalDTO from(Goal goal, String accountName, BigDecimal progressPercent) {
        return new GoalDTO(
                goal.getId(),
                goal.getAccountId(),
                accountName,
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                progressPercent,
                goal.getStatus(),
                goal.getStatus().displayName()
        );
    }
}
