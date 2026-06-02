package com.hfinance.domain.rules;

import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.domain.model.Goal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class GoalRules {
    private GoalRules() {
    }

    public static void validate(Goal goal) {
        if (goal.getName() == null || goal.getName().isBlank()) {
            throw new ValidationException("O nome da meta é obrigatório.");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("O valor deve ser maior que zero.");
        }
        if (goal.getCurrentAmount() == null || goal.getCurrentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("O valor atual não pode ser negativo.");
        }
        if (goal.getDeadline() == null) {
            throw new ValidationException("Selecione uma data.");
        }
    }

    public static BigDecimal progressPercent(BigDecimal currentAmount, BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal current = currentAmount == null ? BigDecimal.ZERO : currentAmount;
        return current.multiply(BigDecimal.valueOf(100)).divide(targetAmount, 2, RoundingMode.HALF_UP);
    }

    public static GoalStatus calculateStatus(Goal goal, LocalDate today) {
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            return GoalStatus.COMPLETED;
        }
        if (goal.getDeadline().isBefore(today)) {
            return GoalStatus.OVERDUE;
        }
        return GoalStatus.IN_PROGRESS;
    }
}
