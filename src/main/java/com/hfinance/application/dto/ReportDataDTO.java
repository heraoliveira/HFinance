package com.hfinance.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportDataDTO(
        List<TransactionDTO> transactions,
        List<BudgetDTO> budgets,
        List<GoalDTO> goals,
        Map<String, BigDecimal> monthlyIncome,
        Map<String, BigDecimal> monthlyExpense,
        Map<String, BigDecimal> monthlyBalance,
        Map<String, BigDecimal> incomeByCategory,
        Map<String, BigDecimal> expenseByCategory,
        Map<String, BigDecimal> byPaymentMethod,
        Map<String, BigDecimal> balanceByAccount,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal totalBalance
) {
    public boolean isEmpty() {
        return transactions.isEmpty() && budgets.isEmpty() && goals.isEmpty();
    }
}
