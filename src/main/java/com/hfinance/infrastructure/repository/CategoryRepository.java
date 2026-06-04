package com.hfinance.infrastructure.repository;

import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    void update(Category category);
    void deactivate(Long id);
    void reactivate(Long id);
    void delete(Long id);
    Optional<Category> findById(Long id);
    List<Category> findAll();
    List<Category> findByType(CategoryType type);
    List<Category> findActiveByType(CategoryType type);
    boolean existsActiveByNameAndType(String name, CategoryType type, Long ignoredId);
    boolean hasTransactions(Long id);
    boolean hasBudgets(Long id);
}
