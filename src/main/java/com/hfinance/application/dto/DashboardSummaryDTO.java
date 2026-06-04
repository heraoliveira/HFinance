package com.hfinance.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummaryDTO(
        BigDecimal totalBalance,
        BigDecimal currentMonthIncome,
        BigDecimal currentMonthExpense,
        BigDecimal currentMonthBalance,
        Map<String, BigDecimal> monthlyIncome,
        Map<String, BigDecimal> monthlyExpense,
        Map<String, BigDecimal> currentMonthExpenseByCategory,
        Map<String, BigDecimal> currentMonthIncomeByCategory,
        List<TransactionDTO> latestTransactions,
        List<AccountDTO> accountBalances,
        List<BudgetDTO> exceededBudgets,
        List<GoalDTO> goals
) {
}
