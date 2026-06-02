package com.hfinance.domain.rules;

import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.BudgetStatus;
import com.hfinance.domain.model.Budget;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BudgetRules {
    private BudgetRules() {
    }

    public static void validate(Budget budget, boolean duplicatedBudget, boolean expenseCategory) {
        if (budget.getName() == null || budget.getName().isBlank()) {
            throw new ValidationException("O nome do orçamento é obrigatório.");
        }
        if (budget.getCategoryId() == null) {
            throw new ValidationException("Selecione uma categoria.");
        }
        if (!expenseCategory) {
            throw new ValidationException("Orçamentos só podem usar categorias de despesa.");
        }
        if (budget.getMonth() < 1 || budget.getMonth() > 12) {
            throw new ValidationException("Informe um mês válido.");
        }
        if (budget.getYear() < 1900) {
            throw new ValidationException("Informe um ano válido.");
        }
        if (budget.getLimitAmount() == null || budget.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("O valor deve ser maior que zero.");
        }
        if (duplicatedBudget) {
            throw new ValidationException("Já existe um orçamento para esta categoria neste mês.");
        }
    }

    public static BigDecimal usagePercent(BigDecimal spent, BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return safe(spent).multiply(BigDecimal.valueOf(100)).divide(limit, 2, RoundingMode.HALF_UP);
    }

    public static BudgetStatus calculateStatus(BigDecimal spent, BigDecimal limit) {
        BigDecimal percent = usagePercent(spent, limit);
        if (percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BudgetStatus.EXCEEDED;
        }
        if (percent.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return BudgetStatus.WARNING;
        }
        return BudgetStatus.ON_TRACK;
    }

    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
