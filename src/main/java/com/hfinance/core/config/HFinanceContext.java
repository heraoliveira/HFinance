package com.hfinance.core.config;

import com.hfinance.application.service.AccountService;
import com.hfinance.application.service.BudgetService;
import com.hfinance.application.service.CategoryService;
import com.hfinance.application.service.DashboardService;
import com.hfinance.application.service.GoalService;
import com.hfinance.application.service.ReportService;
import com.hfinance.application.service.TransactionService;
import com.hfinance.core.backup.BackupService;
import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.database.DatabaseInitializer;
import com.hfinance.core.database.DatabaseRuntime;
import com.hfinance.core.database.DatabaseHealthChecker;
import com.hfinance.core.diagnostics.DiagnosticsService;
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

public class HFinanceContext {
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final DashboardService dashboardService;
    private final ReportService reportService;
    private final AppPaths appPaths;
    private final BackupService backupService;
    private final DatabaseHealthChecker healthChecker;
    private final DiagnosticsService diagnosticsService;

    private HFinanceContext(AccountService accountService, CategoryService categoryService,
                            TransactionService transactionService, BudgetService budgetService,
                            GoalService goalService, DashboardService dashboardService, ReportService reportService,
                            AppPaths appPaths, BackupService backupService, DatabaseHealthChecker healthChecker,
                            DiagnosticsService diagnosticsService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.goalService = goalService;
        this.dashboardService = dashboardService;
        this.reportService = reportService;
        this.appPaths = appPaths;
        this.backupService = backupService;
        this.healthChecker = healthChecker;
        this.diagnosticsService = diagnosticsService;
    }

    public static HFinanceContext create() {
        DatabaseRuntime runtime = new DatabaseInitializer(new DatabaseConfig()).initializeRuntime();
        ConnectionFactory connectionFactory = runtime.connectionFactory();
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
        DashboardService dashboardService = new DashboardService(accountService, transactionService, budgetService,
                goalService, transactionRepository);
        ReportService reportService = new ReportService(transactionService, budgetService, goalService, accountService,
                new ExcelReportExporter(), new CsvTransactionExporter());
        DiagnosticsService diagnosticsService = new DiagnosticsService(runtime.appPaths(), runtime.healthChecker(),
                runtime.backupService());

        return new HFinanceContext(accountService, categoryService, transactionService, budgetService,
                goalService, dashboardService, reportService, runtime.appPaths(), runtime.backupService(),
                runtime.healthChecker(), diagnosticsService);
    }

    public AccountService accountService() {
        return accountService;
    }

    public CategoryService categoryService() {
        return categoryService;
    }

    public TransactionService transactionService() {
        return transactionService;
    }

    public BudgetService budgetService() {
        return budgetService;
    }

    public GoalService goalService() {
        return goalService;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }

    public ReportService reportService() {
        return reportService;
    }

    public AppPaths appPaths() {
        return appPaths;
    }

    public BackupService backupService() {
        return backupService;
    }

    public DatabaseHealthChecker healthChecker() {
        return healthChecker;
    }

    public DiagnosticsService diagnosticsService() {
        return diagnosticsService;
    }
}
