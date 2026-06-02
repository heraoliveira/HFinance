package com.hfinance.infrastructure.repository;

import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    void update(Transaction transaction);
    void delete(Long id);
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    List<Transaction> findLatest(int limit);
    List<Transaction> findByFilter(TransactionFilterDTO filter);
    BigDecimal calculateAccountMovement(Long accountId);
    BigDecimal sumExpensesByCategoryAndMonth(Long categoryId, int month, int year);
    List<Transaction> findBetween(LocalDate startDate, LocalDate endDate);
}
