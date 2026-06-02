package com.hfinance.domain.model;

import com.hfinance.domain.enums.CategoryType;

import java.time.LocalDateTime;

public class Category {
    private Long id;
    private String name;
    private CategoryType type;
    private String color;
    private boolean defaultCategory;
    private LocalDateTime createdAt;

    public Category(Long id, String name, CategoryType type, String color, boolean defaultCategory, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
        this.defaultCategory = defaultCategory;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
