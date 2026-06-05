package com.hfinance.application.service;

import com.hfinance.application.dto.CategoryDTO;
import com.hfinance.core.exception.BusinessException;
import com.hfinance.core.exception.ValidationException;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;
import com.hfinance.infrastructure.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryService {
    private static final String DEFAULT_COLOR = "#00E5FF";

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO create(String name, CategoryType type, String color) {
        validate(name, type, true, null);
        Category category = Category.custom(normalizeName(name), type, normalizeColor(color));
        return CategoryDTO.from(categoryRepository.save(category));
    }

    public CategoryDTO update(Long id, String name, CategoryType type, String color, boolean active) {
        Category existing = findRequired(id);
        validate(name, type, active, id);
        Category updated = new Category(
                existing.getId(),
                normalizeName(name),
                type,
                normalizeColor(color),
                existing.isDefaultCategory(),
                active,
                existing.getCreatedAt(),
                LocalDateTime.now()
        );
        categoryRepository.update(updated);
        return CategoryDTO.from(updated);
    }

    public void deactivate(Long id) {
        findRequired(id);
        categoryRepository.deactivate(id);
    }

    public void reactivate(Long id) {
        Category category = findRequired(id);
        validate(category.getName(), category.getType(), true, id);
        categoryRepository.reactivate(id);
    }

    public void delete(Long id) {
        findRequired(id);
        if (categoryRepository.hasTransactions(id) || categoryRepository.hasBudgets(id)) {
            throw new BusinessException("Não é possível excluir esta categoria porque ela já está em uso. Você pode inativá-la para evitar novos lançamentos.");
        }
        categoryRepository.delete(id);
    }

    public List<CategoryDTO> listAll() {
        return categoryRepository.findAll().stream().map(CategoryDTO::from).toList();
    }

    public List<CategoryDTO> listByType(CategoryType type) {
        return categoryRepository.findByType(type).stream().map(CategoryDTO::from).toList();
    }

    public List<CategoryDTO> listActiveByType(CategoryType type) {
        return categoryRepository.findActiveByType(type).stream().map(CategoryDTO::from).toList();
    }

    public Category getRequired(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Selecione uma categoria."));
    }

    private Category findRequired(Long id) {
        if (id == null) {
            throw new ValidationException("Selecione uma categoria.");
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Selecione uma categoria."));
    }

    private void validate(String name, CategoryType type, boolean active, Long ignoredId) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("O nome da categoria é obrigatório.");
        }
        if (type == null) {
            throw new ValidationException("Selecione o tipo da categoria.");
        }
        if (active && categoryRepository.existsActiveByNameAndType(normalizeName(name), type, ignoredId)) {
            throw new ValidationException("Já existe uma categoria ativa com esse nome para este tipo.");
        }
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    private String normalizeColor(String color) {
        if (color == null || color.isBlank()) {
            return DEFAULT_COLOR;
        }
        String trimmed = color.trim();
        return trimmed.matches("#[0-9a-fA-F]{6}") ? trimmed.toUpperCase() : DEFAULT_COLOR;
    }
}
