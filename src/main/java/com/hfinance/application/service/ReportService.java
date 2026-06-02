package com.hfinance.application.service;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.application.dto.ReportDataDTO;
import com.hfinance.application.dto.ReportFilterDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.infrastructure.report.CsvTransactionExporter;
import com.hfinance.infrastructure.report.ExcelReportExporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final AccountService accountService;
    private final ExcelReportExporter excelReportExporter;
    private final CsvTransactionExporter csvTransactionExporter;

    public ReportService(TransactionService transactionService, BudgetService budgetService,
                         GoalService goalService, AccountService accountService,
                         ExcelReportExporter excelReportExporter, CsvTransactionExporter csvTransactionExporter) {
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.goalService = goalService;
        this.accountService = accountService;
        this.excelReportExporter = excelReportExporter;
        this.csvTransactionExporter = csvTransactionExporter;
    }

    public ReportDataDTO generate(ReportFilterDTO filter) {
        ReportFilterDTO safeFilter = filter == null ? ReportFilterDTO.empty() : filter;
        List<TransactionDTO> transactions = transactionService.filter(safeFilter.toTransactionFilter());
        List<BudgetDTO> budgets = budgetService.listBudgets();
        List<GoalDTO> goals = goalService.listGoals();

        ReportDataDTO data = new ReportDataDTO(
                transactions,
                budgets,
                goals,
                byMonth(transactions, TransactionType.INCOME),
                byMonth(transactions, TransactionType.EXPENSE),
                monthlyBalance(transactions),
                byCategory(transactions, TransactionType.INCOME),
                byCategory(transactions, TransactionType.EXPENSE),
                byPaymentMethod(transactions),
                balanceByAccount(),
                sum(transactions, TransactionType.INCOME),
                sum(transactions, TransactionType.EXPENSE),
                accountService.calculateTotalBalance()
        );
        if (data.isEmpty()) {
            throw new ValidationException("Nenhum dado encontrado para o período selecionado.");
        }
        return data;
    }

    public void exportExcel(Path outputPath, ReportFilterDTO filter) throws IOException {
        excelReportExporter.export(outputPath, generate(filter));
    }

    public void exportCsv(Path outputPath, ReportFilterDTO filter) throws IOException {
        ReportFilterDTO safeFilter = filter == null ? ReportFilterDTO.empty() : filter;
        List<TransactionDTO> transactions = transactionService.filter(safeFilter.toTransactionFilter());
        if (transactions.isEmpty()) {
            throw new ValidationException("Nenhum dado encontrado para o período selecionado.");
        }
        csvTransactionExporter.export(outputPath, transactions);
    }

    private Map<String, BigDecimal> byMonth(List<TransactionDTO> transactions, TransactionType type) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        return transactions.stream()
                .filter(transaction -> transaction.transactionType() == type)
                .collect(Collectors.groupingBy(
                        transaction -> formatter.format(transaction.transactionDate()),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDTO::amount, BigDecimal::add)
                ));
    }

    private Map<String, BigDecimal> monthlyBalance(List<TransactionDTO> transactions) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        transactions.forEach(transaction -> {
            String key = formatter.format(transaction.transactionDate());
            BigDecimal signed = transaction.transactionType() == TransactionType.INCOME
                    ? transaction.amount()
                    : transaction.amount().negate();
            result.merge(key, signed, BigDecimal::add);
        });
        return result;
    }

    private Map<String, BigDecimal> byCategory(List<TransactionDTO> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.transactionType() == type)
                .collect(Collectors.groupingBy(
                        TransactionDTO::categoryName,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDTO::amount, BigDecimal::add)
                ));
    }

    private Map<String, BigDecimal> byPaymentMethod(List<TransactionDTO> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        TransactionDTO::paymentMethodLabel,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, TransactionDTO::amount, BigDecimal::add)
                ));
    }

    private Map<String, BigDecimal> balanceByAccount() {
        return accountService.listAccounts().stream()
                .collect(Collectors.toMap(
                        AccountDTO::name,
                        AccountDTO::currentBalance,
                        BigDecimal::add,
                        LinkedHashMap::new
                ));
    }

    private BigDecimal sum(List<TransactionDTO> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.transactionType() == type)
                .map(TransactionDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
