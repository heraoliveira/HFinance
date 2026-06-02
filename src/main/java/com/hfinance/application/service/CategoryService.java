package com.hfinance.application.service;

import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;
import com.hfinance.infrastructure.repository.CategoryRepository;

import java.util.List;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> listAll() {
        return categoryRepository.findAll().stream().map(CategoryDTO::from).toList();
    }

    public List<CategoryDTO> listByType(CategoryType type) {
        return categoryRepository.findByType(type).stream().map(CategoryDTO::from).toList();
    }

    public Category getRequired(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Selecione uma categoria."));
    }
}
