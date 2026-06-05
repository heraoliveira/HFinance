package com.hfinance.application;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.application.dto.RecurrenceRequest;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.RecurrenceType;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Category;
import com.hfinance.testsupport.TestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void createsAccountAndBlocksDuplicatedActiveAccountName() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);

        AccountDTO account = fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("1000.00"));

        assertThat(account.id()).isNotNull();
        assertThat(account.currentBalance()).isEqualByComparingTo("1000.00");
        assertThatThrownBy(() -> fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe uma conta ativa com este nome.");
    }

    @Test
    void deactivatesAccountWithTransactions() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category income = fixture.incomeCategory();
        fixture.transactionService().create(account.id(), income.getId(), LocalDate.now(), TransactionType.INCOME,
                PaymentMethod.PIX, "Receita", new BigDecimal("100.00"));

        fixture.accountService().deactivate(account.id());

        assertThat(fixture.accountService().listAccounts())
                .filteredOn(AccountDTO::active)
                .isEmpty();
    }

    @Test
    void createsIncomeExpenseAndCalculatesBalances() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category income = fixture.incomeCategory();
        Category expense = fixture.expenseCategory();

        fixture.transactionService().create(account.id(), income.getId(), LocalDate.now(), TransactionType.INCOME,
                PaymentMethod.PIX, "Salário", new BigDecimal("500.00"));
        fixture.transactionService().create(account.id(), expense.getId(), LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.CREDIT_CARD, "Mercado", new BigDecimal("200.00"));

        AccountDTO updated = fixture.accountService().listAccounts().get(0);
        assertThat(updated.currentBalance()).isEqualByComparingTo("1300.00");
        assertThat(fixture.accountService().calculateTotalBalance()).isEqualByComparingTo("1300.00");
    }

    @Test
    void editsAndDeletesTransactionReflectingBalanceImmediately() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category expense = fixture.expenseCategory();
        TransactionDTO transaction = fixture.transactionService().create(account.id(), expense.getId(), LocalDate.now(),
                TransactionType.EXPENSE, PaymentMethod.PIX, "Compra", new BigDecimal("100.00"));

        fixture.transactionService().update(transaction.id(), account.id(), expense.getId(), LocalDate.now(),
                TransactionType.EXPENSE, PaymentMethod.PIX, "Compra atualizada", new BigDecimal("250.00"));
        assertThat(fixture.accountService().listAccounts().get(0).currentBalance()).isEqualByComparingTo("750.00");

        fixture.transactionService().delete(transaction.id());
        assertThat(fixture.accountService().listAccounts().get(0).currentBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void createsBudgetBlocksDuplicateAndCalculatesUsage() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category expense = fixture.expenseCategory();
        LocalDate today = LocalDate.now();
        fixture.transactionService().create(account.id(), expense.getId(), today, TransactionType.EXPENSE,
                PaymentMethod.PIX, "Despesa", new BigDecimal("150.00"));

        BudgetDTO budget = fixture.budgetService().create(expense.getId(), "Alimentação",
                today.getMonthValue(), today.getYear(), new BigDecimal("200.00"));

        assertThat(budget.spentAmount()).isEqualByComparingTo("150.00");
        assertThat(budget.usagePercent()).isEqualByComparingTo("75.00");
        assertThatThrownBy(() -> fixture.budgetService().create(expense.getId(), "Duplicado",
                today.getMonthValue(), today.getYear(), new BigDecimal("300.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um orçamento para esta categoria neste mês.");
    }

    @Test
    void createsAndUpdatesGoalProgress() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);

        GoalDTO goal = fixture.goalService().create(null, "Reserva", new BigDecimal("1000.00"),
                new BigDecimal("200.00"), LocalDate.now().plusMonths(1));
        assertThat(goal.progressPercent()).isEqualByComparingTo("20.00");
        assertThat(goal.status()).isEqualTo(GoalStatus.IN_PROGRESS);

        GoalDTO updated = fixture.goalService().update(goal.id(), null, "Reserva", new BigDecimal("1000.00"),
                new BigDecimal("1000.00"), LocalDate.now().plusMonths(1));
        assertThat(updated.progressPercent()).isEqualByComparingTo("100.00");
        assertThat(updated.status()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void managesCustomCategoriesAndBlocksActiveDuplicates() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);

        CategoryDTO income = fixture.categoryService().create("Renda extra", CategoryType.INCOME, "#00E5FF");
        CategoryDTO expense = fixture.categoryService().create("Renda extra", CategoryType.EXPENSE, "#FFD700");

        assertThat(income.defaultCategory()).isFalse();
        assertThat(expense.type()).isEqualTo(CategoryType.EXPENSE);
        assertThat(fixture.categoryService().listActiveByType(CategoryType.INCOME))
                .extracting(CategoryDTO::name)
                .contains("Renda extra");
        assertThatThrownBy(() -> fixture.categoryService().create(" renda   extra ", CategoryType.INCOME, "#FFFFFF"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe uma categoria ativa com esse nome para este tipo.");
    }

    @Test
    void inactivatesUsedCategoryAndKeepsHistoricalTransactionsVisible() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        CategoryDTO category = fixture.categoryService().create("Assinatura premium", CategoryType.EXPENSE, "#FFD700");

        fixture.transactionService().create(account.id(), category.id(), LocalDate.now(), TransactionType.EXPENSE,
                PaymentMethod.CREDIT_CARD, "Streaming", new BigDecimal("39.90"));
        fixture.categoryService().deactivate(category.id());

        assertThat(fixture.categoryService().listActiveByType(CategoryType.EXPENSE))
                .extracting(CategoryDTO::id)
                .doesNotContain(category.id());
        assertThat(fixture.transactionService().listTransactions())
                .extracting(TransactionDTO::categoryName)
                .contains("Assinatura premium");
        assertThatThrownBy(() -> fixture.categoryService().delete(category.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível excluir esta categoria porque ela já está em uso. Você pode inativá-la para evitar novos lançamentos.");
    }

    @Test
    void removesDeprecatedSeedCategoriesAndDeletesUnusedDefaultCategory() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);

        assertThat(fixture.categoryService().listAll())
                .extracting(CategoryDTO::name)
                .doesNotContain("Freelance", "Presente");

        CategoryDTO defaultCategory = fixture.categoryService().listAll().stream()
                .filter(CategoryDTO::defaultCategory)
                .findFirst()
                .orElseThrow();

        fixture.categoryService().delete(defaultCategory.id());

        assertThat(fixture.categoryService().listAll())
                .extracting(CategoryDTO::id)
                .doesNotContain(defaultCategory.id());
    }

    @Test
    void blocksDeletingUsedDefaultCategoryAndAllowsInactivation() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category income = fixture.incomeCategory();

        fixture.transactionService().create(account.id(), income.getId(), LocalDate.now(), TransactionType.INCOME,
                PaymentMethod.PIX, "Receita", new BigDecimal("100.00"));

        assertThatThrownBy(() -> fixture.categoryService().delete(income.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível excluir esta categoria porque ela já está em uso. Você pode inativá-la para evitar novos lançamentos.");

        fixture.categoryService().deactivate(income.getId());

        assertThat(fixture.categoryService().listActiveByType(CategoryType.INCOME))
                .extracting(CategoryDTO::id)
                .doesNotContain(income.getId());
    }

    @Test
    void budgetRequiresActiveExpenseCategory() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        CategoryDTO category = fixture.categoryService().create("Viagens", CategoryType.EXPENSE, "#00E5FF");
        fixture.categoryService().deactivate(category.id());

        assertThatThrownBy(() -> fixture.budgetService().create(category.id(), "Viagens",
                LocalDate.now().getMonthValue(), LocalDate.now().getYear(), new BigDecimal("500.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Selecione uma categoria de despesa ativa.");
    }

    @Test
    void createsRecurringTransactionsWithExpectedDatesAndMetadata() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category expense = fixture.expenseCategory();

        List<TransactionDTO> transactions = fixture.transactionService().createRecurring(account.id(), expense.getId(),
                LocalDate.of(2026, 1, 31), TransactionType.EXPENSE, PaymentMethod.PIX,
                "Aluguel", new BigDecimal("1200.00"),
                new RecurrenceRequest(RecurrenceType.MONTHLY, 3));

        assertThat(transactions).hasSize(4);
        assertThat(transactions).extracting(TransactionDTO::transactionDate)
                .containsExactly(LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28),
                        LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 30));
        assertThat(transactions).allSatisfy(transaction -> {
            assertThat(transaction.recurrenceGroupId()).isNotBlank();
            assertThat(transaction.recurrenceType()).isEqualTo(RecurrenceType.MONTHLY);
            assertThat(transaction.recurrenceTotal()).isEqualTo(4);
            assertThat(transaction.accountId()).isEqualTo(account.id());
            assertThat(transaction.categoryId()).isEqualTo(expense.getId());
            assertThat(transaction.amount()).isEqualByComparingTo("1200.00");
        });
    }

    @Test
    void createsWeeklyAndYearlyRecurringTransactions() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category income = fixture.incomeCategory();

        List<TransactionDTO> weekly = fixture.transactionService().createRecurring(account.id(), income.getId(),
                LocalDate.of(2026, 6, 4), TransactionType.INCOME, PaymentMethod.PIX,
                "Freelance", new BigDecimal("300.00"),
                new RecurrenceRequest(RecurrenceType.WEEKLY, 2));
        List<TransactionDTO> yearly = fixture.transactionService().createRecurring(account.id(), income.getId(),
                LocalDate.of(2024, 2, 29), TransactionType.INCOME, PaymentMethod.PIX,
                "Bônus", new BigDecimal("1000.00"),
                new RecurrenceRequest(RecurrenceType.YEARLY, 2));

        assertThat(weekly).extracting(TransactionDTO::transactionDate)
                .containsExactly(LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 11),
                        LocalDate.of(2026, 6, 18));
        assertThat(yearly).extracting(TransactionDTO::transactionDate)
                .containsExactly(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28),
                        LocalDate.of(2026, 2, 28));
    }

    @Test
    void updatesSelectedAndFutureRecurringTransactionsWithoutChangingPreviousHistory() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category expense = fixture.expenseCategory();

        List<TransactionDTO> transactions = fixture.transactionService().createRecurring(account.id(), expense.getId(),
                LocalDate.of(2026, 1, 31), TransactionType.EXPENSE, PaymentMethod.PIX,
                "Aluguel", new BigDecimal("1200.00"),
                new RecurrenceRequest(RecurrenceType.MONTHLY, 3));

        List<TransactionDTO> updated = fixture.transactionService().updateRecurringFrom(transactions.get(1).id(),
                account.id(), expense.getId(), LocalDate.of(2026, 2, 27), TransactionType.EXPENSE,
                PaymentMethod.CREDIT_CARD, "Aluguel ajustado", new BigDecimal("1300.00"));

        assertThat(updated).hasSize(3);
        assertThat(fixture.transactionRepository().findById(transactions.get(0).id()).orElseThrow())
                .satisfies(transaction -> {
                    assertThat(transaction.getTransactionDate()).isEqualTo(LocalDate.of(2026, 1, 31));
                    assertThat(transaction.getDescription()).isEqualTo("Aluguel");
                    assertThat(transaction.getAmount()).isEqualByComparingTo("1200.00");
                });
        assertThat(fixture.transactionRepository().findById(transactions.get(1).id()).orElseThrow())
                .satisfies(transaction -> {
                    assertThat(transaction.getTransactionDate()).isEqualTo(LocalDate.of(2026, 2, 27));
                    assertThat(transaction.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
                    assertThat(transaction.getDescription()).isEqualTo("Aluguel ajustado");
                    assertThat(transaction.getAmount()).isEqualByComparingTo("1300.00");
                });
        assertThat(fixture.transactionRepository().findById(transactions.get(2).id()).orElseThrow())
                .satisfies(transaction -> {
                    assertThat(transaction.getTransactionDate()).isEqualTo(LocalDate.of(2026, 3, 31));
                    assertThat(transaction.getDescription()).isEqualTo("Aluguel ajustado");
                    assertThat(transaction.getAmount()).isEqualByComparingTo("1300.00");
                });
    }

    @Test
    void validatesRecurringTransactions() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        AccountDTO account = createAccount(fixture);
        Category expense = fixture.expenseCategory();

        assertThatThrownBy(() -> fixture.transactionService().createRecurring(account.id(), expense.getId(),
                LocalDate.now(), TransactionType.EXPENSE, PaymentMethod.PIX, "Inválida", new BigDecimal("10.00"),
                new RecurrenceRequest(RecurrenceType.MONTHLY, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Informe a quantidade de repetições.");
        assertThatThrownBy(() -> fixture.transactionService().createRecurring(account.id(), expense.getId(),
                LocalDate.now(), TransactionType.EXPENSE, PaymentMethod.PIX, "Inválida", new BigDecimal("10.00"),
                new RecurrenceRequest(RecurrenceType.MONTHLY, 0)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A quantidade de repetições deve ser maior que zero.");
        assertThatThrownBy(() -> fixture.transactionService().createRecurring(account.id(), expense.getId(),
                LocalDate.now(), TransactionType.EXPENSE, PaymentMethod.PIX, "Inválida", new BigDecimal("10.00"),
                new RecurrenceRequest(RecurrenceType.MONTHLY, 121)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A quantidade de repetições deve ser no máximo 120.");
    }

    private AccountDTO createAccount(TestSupport.Fixture fixture) {
        return fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("1000.00"));
    }
}
