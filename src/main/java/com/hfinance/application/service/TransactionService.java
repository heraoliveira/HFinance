package com.hfinance.application.service;

import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Account;
import com.hfinance.domain.model.Category;
import com.hfinance.domain.model.Transaction;
import com.hfinance.domain.rules.TransactionRules;
import com.hfinance.infrastructure.repository.AccountRepository;
import com.hfinance.infrastructure.repository.CategoryRepository;
import com.hfinance.infrastructure.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    public TransactionDTO create(Long accountId, Long categoryId, LocalDate date, TransactionType type,
                                 PaymentMethod paymentMethod, String description, BigDecimal amount) {
        Transaction transaction = Transaction.newTransaction(accountId, categoryId, date, type, paymentMethod, description, amount);
        validateTransaction(transaction);
        return toDTO(transactionRepository.save(transaction));
    }

    public TransactionDTO update(Long id, Long accountId, Long categoryId, LocalDate date, TransactionType type,
                                 PaymentMethod paymentMethod, String description, BigDecimal amount) {
        Transaction existing = getRequired(id);
        Transaction updated = new Transaction(id, accountId, categoryId, date, type, paymentMethod,
                description == null ? "" : description, amount, existing.getCreatedAt(), LocalDateTime.now());
        validateTransaction(updated);
        transactionRepository.update(updated);
        return toDTO(updated);
    }

    public void delete(Long id) {
        getRequired(id);
        transactionRepository.delete(id);
    }

    public Transaction getRequired(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Transação não encontrada."));
    }

    public List<TransactionDTO> listTransactions() {
        return mapTransactions(transactionRepository.findAll());
    }

    public List<TransactionDTO> filter(TransactionFilterDTO filter) {
        return mapTransactions(transactionRepository.findByFilter(filter));
    }

    public List<TransactionDTO> latest(int limit) {
        return mapTransactions(transactionRepository.findLatest(limit));
    }

    public List<TransactionDTO> mapTransactions(List<Transaction> transactions) {
        Map<Long, String> accountNames = new HashMap<>();
        Map<Long, String> categoryNames = new HashMap<>();
        return transactions.stream()
                .map(transaction -> TransactionDTO.from(
                        transaction,
                        accountNames.computeIfAbsent(transaction.getAccountId(), this::accountName),
                        categoryNames.computeIfAbsent(transaction.getCategoryId(), this::categoryName)))
                .toList();
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction.getAccountId() == null) {
            throw new ValidationException("Selecione uma conta.");
        }
        if (transaction.getCategoryId() == null) {
            throw new ValidationException("Selecione uma categoria.");
        }
        Account account = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new ValidationException("Selecione uma conta."));
        Category category = categoryRepository.findById(transaction.getCategoryId())
                .orElseThrow(() -> new ValidationException("Selecione uma categoria."));
        TransactionRules.validate(transaction, account.isActive());
        CategoryType expectedCategoryType = transaction.getTransactionType() == TransactionType.INCOME
                ? CategoryType.INCOME
                : CategoryType.EXPENSE;
        if (category.getType() != expectedCategoryType) {
            throw new ValidationException("A categoria selecionada não corresponde ao tipo da transação.");
        }
    }

    private String accountName(Long accountId) {
        return accountRepository.findById(accountId).map(Account::getName).orElse("Conta removida");
    }

    private String categoryName(Long categoryId) {
        return categoryRepository.findById(categoryId).map(Category::getName).orElse("Categoria removida");
    }

    public TransactionDTO toDTO(Transaction transaction) {
        return TransactionDTO.from(transaction, accountName(transaction.getAccountId()), categoryName(transaction.getCategoryId()));
    }
}
