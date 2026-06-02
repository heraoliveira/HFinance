package com.hfinance.infrastructure.repository;

import com.hfinance.domain.model.Budget;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository {
    Budget save(Budget budget);
    void update(Budget budget);
    void delete(Long id);
    Optional<Budget> findById(Long id);
    List<Budget> findAll();
    boolean existsByCategoryMonthYear(Long categoryId, int month, int year, Long excludedId);
}
