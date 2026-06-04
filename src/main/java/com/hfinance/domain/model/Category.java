package com.hfinance.domain.model;

import com.hfinance.domain.enums.CategoryType;

import java.time.LocalDateTime;

public class Category {
    private Long id;
    private String name;
    private CategoryType type;
    private String color;
    private boolean defaultCategory;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Category(Long id, String name, CategoryType type, String color, boolean defaultCategory, LocalDateTime createdAt) {
        this(id, name, type, color, defaultCategory, true, createdAt, createdAt);
    }

    public Category(Long id, String name, CategoryType type, String color, boolean defaultCategory,
                    boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
        this.defaultCategory = defaultCategory;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Category custom(String name, CategoryType type, String color) {
        LocalDateTime now = LocalDateTime.now();
        return new Category(null, name, type, color, false, true, now, now);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDefaultCategory() {
        return defaultCategory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
