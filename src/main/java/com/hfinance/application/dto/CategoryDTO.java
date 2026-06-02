package com.hfinance.application.dto;

import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;

public record CategoryDTO(
        Long id,
        String name,
        CategoryType type,
        String typeLabel,
        String color,
        boolean defaultCategory
) {
    public static CategoryDTO from(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getType(),
                category.getType().displayName(), category.getColor(), category.isDefaultCategory());
    }
}
