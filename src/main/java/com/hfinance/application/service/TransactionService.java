package com.hfinance.application.service;

import com.hfinance.application.dto.RecurrenceRequest;
import com.hfinance.application.dto.TransactionDTO;
import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.RecurrenceType;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionService {
    private static final int MAX_RECURRENCE_OCCURRENCES = 120;

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
        return createRecurring(accountId, categoryId, date, type, paymentMethod, description, amount,
                RecurrenceRequest.none()).get(0);
    }

    public List<TransactionDTO> createRecurring(Long accountId, Long categoryId, LocalDate date, TransactionType type,
                                                PaymentMethod paymentMethod, String description, BigDecimal amount,
                                                RecurrenceRequest recurrence) {
        RecurrenceRequest safeRecurrence = recurrence == null ? RecurrenceRequest.none() : recurrence;
        int occurrences = calculateOccurrences(date, safeRecurrence);
        String recurrenceGroupId = safeRecurrence.recurring() ? UUID.randomUUID().toString() : null;
        RecurrenceType recurrenceType = safeRecurrence.safeType();
        List<TransactionDTO> saved = new ArrayList<>();
        for (int index = 1; index <= occurrences; index++) {
            LocalDate occurrenceDate = recurrenceType.nextDate(date, index - 1);
            Transaction transaction = Transaction.newTransaction(accountId, categoryId, occurrenceDate, type,
                    paymentMethod, description, amount);
            if (safeRecurrence.recurring()) {
                transaction.setRecurrenceGroupId(recurrenceGroupId);
                transaction.setRecurrenceType(recurrenceType);
                transaction.setRecurrenceIndex(index);
                transaction.setRecurrenceTotal(occurrences);
            }
            validateTransaction(transaction, true);
            saved.add(toDTO(transactionRepository.save(transaction)));
        }
        return saved;
    }

    public TransactionDTO update(Long id, Long accountId, Long categoryId, LocalDate date, TransactionType type,
                                 PaymentMethod paymentMethod, String description, BigDecimal amount) {
        Transaction existing = getRequired(id);
        Transaction updated = new Transaction(id, accountId, categoryId, date, type, paymentMethod,
                description == null ? "" : description, amount, existing.getRecurrenceGroupId(),
                existing.getRecurrenceType(), existing.getRecurrenceIndex(), existing.getRecurrenceTotal(),
                existing.getCreatedAt(), LocalDateTime.now());
        validateTransaction(updated, false);
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

    private int calculateOccurrences(LocalDate startDate, RecurrenceRequest recurrence) {
        if (!recurrence.recurring()) {
            return 1;
        }
        if (startDate == null) {
            throw new ValidationException("Selecione uma data.");
        }
        Integer repetitions = recurrence.repetitions();
        LocalDate endDate = recurrence.endDate();
        if (repetitions == null && endDate == null) {
            throw new ValidationException("Informe a quantidade de repetições ou a data final da recorrência.");
        }
        if (repetitions != null && repetitions <= 0) {
            throw new ValidationException("A quantidade de repetições deve ser maior que zero.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ValidationException("A data final da recorrência deve ser igual ou posterior à data inicial.");
        }
        int occurrenceLimit = repetitions == null ? MAX_RECURRENCE_OCCURRENCES : repetitions;
        if (occurrenceLimit > MAX_RECURRENCE_OCCURRENCES) {
            throw new ValidationException("A recorrência permite no máximo 120 ocorrências.");
        }
        if (endDate == null) {
            return occurrenceLimit;
        }
        int occurrencesByEndDate = 0;
        LocalDate occurrenceDate = startDate;
        while (!occurrenceDate.isAfter(endDate) && occurrencesByEndDate < MAX_RECURRENCE_OCCURRENCES) {
            occurrencesByEndDate++;
            occurrenceDate = recurrence.safeType().nextDate(startDate, occurrencesByEndDate);
        }
        if (occurrencesByEndDate == 0) {
            throw new ValidationException("A recorrência deve gerar pelo menos uma transação.");
        }
        return Math.min(occurrenceLimit, occurrencesByEndDate);
    }

    private void validateTransaction(Transaction transaction, boolean requireActiveCategory) {
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
        if (requireActiveCategory && !category.isActive()) {
            throw new ValidationException("Selecione uma categoria ativa.");
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
