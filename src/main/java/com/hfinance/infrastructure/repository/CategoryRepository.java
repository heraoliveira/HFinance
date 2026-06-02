package com.hfinance.infrastructure.repository;

import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    List<Category> findByType(CategoryType type);
}
