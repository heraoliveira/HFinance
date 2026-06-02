package com.hfinance.domain;

import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.BudgetStatus;
import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Goal;
import com.hfinance.domain.model.Transaction;
import com.hfinance.domain.rules.BudgetRules;
import com.hfinance.domain.rules.GoalRules;
import com.hfinance.domain.rules.TransactionRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainRulesTest {
    @Test
    void validatesPositiveTransactionAmount() {
        Transaction transaction = Transaction.newTransaction(1L, 1L, LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.PIX, "Teste", BigDecimal.ZERO);

        assertThatThrownBy(() -> TransactionRules.validate(transaction, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage("O valor deve ser maior que zero.");
    }

    @Test
    void calculatesBudgetStatus() {
        assertThat(BudgetRules.calculateStatus(new BigDecimal("79.99"), new BigDecimal("100.00")))
                .isEqualTo(BudgetStatus.ON_TRACK);
        assertThat(BudgetRules.calculateStatus(new BigDecimal("80.00"), new BigDecimal("100.00")))
                .isEqualTo(BudgetStatus.WARNING);
        assertThat(BudgetRules.calculateStatus(new BigDecimal("100.01"), new BigDecimal("100.00")))
                .isEqualTo(BudgetStatus.EXCEEDED);
    }

    @Test
    void calculatesGoalProgressAndStatus() {
        Goal goal = Goal.newGoal(null, "Reserva", new BigDecimal("1000.00"),
                new BigDecimal("250.00"), LocalDate.now().plusDays(10));

        assertThat(GoalRules.progressPercent(goal.getCurrentAmount(), goal.getTargetAmount()))
                .isEqualByComparingTo("25.00");
        assertThat(GoalRules.calculateStatus(goal, LocalDate.now())).isEqualTo(GoalStatus.IN_PROGRESS);

        goal.setCurrentAmount(new BigDecimal("1000.00"));
        assertThat(GoalRules.calculateStatus(goal, LocalDate.now())).isEqualTo(GoalStatus.COMPLETED);

        goal.setCurrentAmount(new BigDecimal("500.00"));
        goal.setDeadline(LocalDate.now().minusDays(1));
        assertThat(GoalRules.calculateStatus(goal, LocalDate.now())).isEqualTo(GoalStatus.OVERDUE);
    }

    @Test
    void validatesMaximumDescriptionLength() {
        Transaction transaction = Transaction.newTransaction(1L, 1L, LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.PIX, "x".repeat(256), new BigDecimal("10.00"));

        assertThatThrownBy(() -> TransactionRules.validate(transaction, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage("A descrição deve ter no máximo 255 caracteres.");
    }
}
