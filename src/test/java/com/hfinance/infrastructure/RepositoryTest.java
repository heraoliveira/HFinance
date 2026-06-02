package com.hfinance.infrastructure;

import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Account;
import com.hfinance.domain.model.Category;
import com.hfinance.domain.model.Transaction;
import com.hfinance.testsupport.TestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndFindsAccountAndTransaction() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        Account account = fixture.accountRepository().save(Account.newAccount("Principal", "Banco",
                AccountType.CHECKING_ACCOUNT, new BigDecimal("100.00")));
        Category category = fixture.incomeCategory();
        Transaction transaction = fixture.transactionRepository().save(Transaction.newTransaction(account.getId(),
                category.getId(), LocalDate.now(), TransactionType.INCOME, PaymentMethod.PIX,
                "Receita", new BigDecimal("50.00")));

        assertThat(fixture.accountRepository().findById(account.getId())).isPresent();
        assertThat(fixture.transactionRepository().findById(transaction.getId())).isPresent();
    }

    @Test
    void filtersTransactionsByDateTypePaymentDescriptionAndAmount() {
        TestSupport.Fixture fixture = TestSupport.fixture(tempDir);
        Account account = fixture.accountRepository().save(Account.newAccount("Principal", "Banco",
                AccountType.CHECKING_ACCOUNT, new BigDecimal("0.00")));
        Category income = fixture.incomeCategory();
        Category expense = fixture.expenseCategory();

        fixture.transactionRepository().save(Transaction.newTransaction(account.getId(), income.getId(),
                LocalDate.of(2026, 1, 10), TransactionType.INCOME, PaymentMethod.PIX,
                "Salário mensal", new BigDecimal("3000.00")));
        fixture.transactionRepository().save(Transaction.newTransaction(account.getId(), expense.getId(),
                LocalDate.of(2026, 1, 20), TransactionType.EXPENSE, PaymentMethod.CREDIT_CARD,
                "Mercado", new BigDecimal("350.00")));
        fixture.transactionRepository().save(Transaction.newTransaction(account.getId(), expense.getId(),
                LocalDate.of(2026, 2, 5), TransactionType.EXPENSE, PaymentMethod.CASH,
                "Transporte", new BigDecimal("80.00")));

        assertThat(fixture.transactionRepository().findByFilter(new TransactionFilterDTO(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                null, null, null, null, null, null, null, null, null))).hasSize(2);
        assertThat(fixture.transactionRepository().findByFilter(new TransactionFilterDTO(
                null, null, null, null, null, null, TransactionType.EXPENSE, null, null, null, null))).hasSize(2);
        assertThat(fixture.transactionRepository().findByFilter(new TransactionFilterDTO(
                null, null, null, null, null, null, null, PaymentMethod.CREDIT_CARD, null, null, null))).hasSize(1);
        assertThat(fixture.transactionRepository().findByFilter(new TransactionFilterDTO(
                null, null, null, null, null, null, null, null, "merc", null, null))).hasSize(1);
        assertThat(fixture.transactionRepository().findByFilter(new TransactionFilterDTO(
                null, null, null, null, null, null, null, null, null,
                new BigDecimal("100.00"), new BigDecimal("500.00")))).hasSize(1);
    }
}
