package com.hfinance.application.service;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.DashboardSummaryDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Transaction;
import com.hfinance.infrastructure.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final TransactionRepository transactionRepository;

    public DashboardService(AccountService accountService, TransactionService transactionService,
                            BudgetService budgetService, GoalService goalService,
                            TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.goalService = goalService;
        this.transactionRepository = transactionRepository;
    }

    public DashboardSummaryDTO summary() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        List<Transaction> monthTransactions = transactionRepository.findBetween(monthStart, monthEnd);
        List<TransactionDTO> monthTransactionDTOs = transactionService.mapTransactions(monthTransactions);

        BigDecimal monthIncome = sumByType(monthTransactions, TransactionType.INCOME);
        BigDecimal monthExpense = sumByType(monthTransactions, TransactionType.EXPENSE);
        Map<String, BigDecimal> monthlyIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> monthlyExpense = new LinkedHashMap<>();
        buildMonthlySeries(today, monthlyIncome, monthlyExpense);

        List<BudgetDTO> exceeded = budgetService.listBudgets().stream()
                .filter(budget -> budget.usagePercent().compareTo(BigDecimal.valueOf(100)) > 0)
                .toList();
        List<GoalDTO> goals = goalService.listGoals();
        List<AccountDTO> accountBalances = accountService.listAccounts();
        List<TransactionDTO> latestTransactions = transactionService.latest(8);

        return new DashboardSummaryDTO(
                accountService.calculateTotalBalance(),
                monthIncome,
                monthExpense,
                monthIncome.subtract(monthExpense),
                monthlyIncome,
                monthlyExpense,
                byCategory(monthTransactionDTOs, TransactionType.EXPENSE),
                byCategory(monthTransactionDTOs, TransactionType.INCOME),
                latestTransactions,
                accountBalances,
                exceeded,
                goals
        );
    }

    private void buildMonthlySeries(LocalDate today, Map<String, BigDecimal> income, Map<String, BigDecimal> expense) {
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i).withDayOfMonth(1);
            String label = labelFormatter.format(month);
            List<Transaction> transactions = transactionRepository.findBetween(month, month.withDayOfMonth(month.lengthOfMonth()));
            income.put(label, sumByType(transactions, TransactionType.INCOME));
            expense.put(label, sumByType(transactions, TransactionType.EXPENSE));
        }
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> byCategory(List<TransactionDTO> transactions, TransactionType type) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        transactions.stream()
                .filter(transaction -> transaction.transactionType() == type)
                .forEach(transaction -> result.merge(transaction.categoryName(), transaction.amount(), BigDecimal::add));
        return result;
    }
}
