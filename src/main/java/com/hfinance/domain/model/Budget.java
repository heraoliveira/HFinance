package com.hfinance.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Budget {
    private Long id;
    private Long categoryId;
    private String name;
    private int month;
    private int year;
    private BigDecimal limitAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Budget(Long id, Long categoryId, String name, int month, int year, BigDecimal limitAmount,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.month = month;
        this.year = year;
        this.limitAmount = limitAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Budget newBudget(Long categoryId, String name, int month, int year, BigDecimal limitAmount) {
        LocalDateTime now = LocalDateTime.now();
        return new Budget(null, categoryId, name, month, year, limitAmount, now, now);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
