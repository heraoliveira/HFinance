package com.hfinance.application;

import com.hfinance.application.dto.AccountDTO;
import com.hfinance.application.dto.BudgetDTO;
import com.hfinance.application.dto.GoalDTO;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Category;
import com.hfinance.testsupport.TestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

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

    private AccountDTO createAccount(TestSupport.Fixture fixture) {
        return fixture.accountService().create("Principal", "Banco", AccountType.CHECKING_ACCOUNT,
                new BigDecimal("1000.00"));
    }
}
