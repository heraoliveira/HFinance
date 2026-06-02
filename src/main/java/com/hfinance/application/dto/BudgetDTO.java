package com.hfinance.application.dto;

import com.hfinance.domain.enums.BudgetStatus;
import com.hfinance.domain.model.Budget;

import java.math.BigDecimal;

public record BudgetDTO(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        int month,
        int year,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal usagePercent,
        BudgetStatus status,
        String statusLabel
) {
    public static BudgetDTO from(Budget budget, String categoryName, BigDecimal spentAmount,
                                 BigDecimal usagePercent, BudgetStatus status) {
        return new BudgetDTO(
                budget.getId(),
                budget.getCategoryId(),
                categoryName,
                budget.getName(),
                budget.getMonth(),
                budget.getYear(),
                budget.getLimitAmount(),
                spentAmount,
                usagePercent,
                status,
                status.displayName()
        );
    }
}
