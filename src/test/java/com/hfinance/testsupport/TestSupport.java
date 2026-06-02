package com.hfinance.testsupport;

import com.hfinance.application.service.AccountService;
import com.hfinance.application.service.BudgetService;
import com.hfinance.application.service.CategoryService;
import com.hfinance.application.service.GoalService;
import com.hfinance.application.service.ReportService;
import com.hfinance.application.service.TransactionService;
import com.hfinance.core.config.DatabaseConfig;
import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.database.DatabaseInitializer;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;
import com.hfinance.infrastructure.report.CsvTransactionExporter;
import com.hfinance.infrastructure.report.ExcelReportExporter;
import com.hfinance.infrastructure.repository.AccountRepository;
import com.hfinance.infrastructure.repository.BudgetRepository;
import com.hfinance.infrastructure.repository.CategoryRepository;
import com.hfinance.infrastructure.repository.GoalRepository;
import com.hfinance.infrastructure.repository.TransactionRepository;
import com.hfinance.infrastructure.repository.jdbc.JdbcAccountRepository;
import com.hfinance.infrastructure.repository.jdbc.JdbcBudgetRepository;
import com.hfinance.infrastructure.repository.jdbc.JdbcCategoryRepository;
import com.hfinance.infrastructure.repository.jdbc.JdbcGoalRepository;
import com.hfinance.infrastructure.repository.jdbc.JdbcTransactionRepository;

import java.nio.file.Path;

public final class TestSupport {
    private TestSupport() {
    }

    public static Fixture fixture(Path tempDir) {
        ConnectionFactory connectionFactory = new DatabaseInitializer(
                new DatabaseConfig(tempDir.resolve("hfinance-test.db"))).initialize();
        AccountRepository accountRepository = new JdbcAccountRepository(connectionFactory);
        CategoryRepository categoryRepository = new JdbcCategoryRepository(connectionFactory);
        TransactionRepository transactionRepository = new JdbcTransactionRepository(connectionFactory);
        BudgetRepository budgetRepository = new JdbcBudgetRepository(connectionFactory);
        GoalRepository goalRepository = new JdbcGoalRepository(connectionFactory);

        AccountService accountService = new AccountService(accountRepository, transactionRepository);
        CategoryService categoryService = new CategoryService(categoryRepository);
        TransactionService transactionService = new TransactionService(transactionRepository, accountRepository, categoryRepository);
        BudgetService budgetService = new BudgetService(budgetRepository, categoryRepository, transactionRepository);
        GoalService goalService = new GoalService(goalRepository, accountRepository);
        ReportService reportService = new ReportService(transactionService, budgetService, goalService, accountService,
                new ExcelReportExporter(), new CsvTransactionExporter());

        return new Fixture(accountRepository, categoryRepository, transactionRepository, budgetRepository, goalRepository,
                accountService, categoryService, transactionService, budgetService, goalService, reportService);
    }

    public record Fixture(
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            BudgetRepository budgetRepository,
            GoalRepository goalRepository,
            AccountService accountService,
            CategoryService categoryService,
            TransactionService transactionService,
            BudgetService budgetService,
            GoalService goalService,
            ReportService reportService
    ) {
        public Category incomeCategory() {
            return categoryRepository.findByType(CategoryType.INCOME).get(0);
        }

        public Category expenseCategory() {
            return categoryRepository.findByType(CategoryType.EXPENSE).get(0);
        }
    }
}
