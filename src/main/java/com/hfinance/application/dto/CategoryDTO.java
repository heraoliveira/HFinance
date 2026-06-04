package com.hfinance.application.dto;

import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;

public record CategoryDTO(
        Long id,
        String name,
        CategoryType type,
        String typeLabel,
        String color,
        boolean defaultCategory,
        boolean active,
        String originLabel,
        String statusLabel
) {
    public static CategoryDTO from(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getType(),
                category.getType().displayName(), category.getColor(), category.isDefaultCategory(),
                category.isActive(),
                category.isDefaultCategory() ? "Padrão" : "Personalizada",
                category.isActive() ? "Ativa" : "Inativa");
    }
}
