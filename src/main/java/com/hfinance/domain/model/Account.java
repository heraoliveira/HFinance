package com.hfinance.domain.model;

import com.hfinance.domain.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private Long id;
    private String name;
    private String institution;
    private AccountType type;
    private BigDecimal initialBalance;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account(Long id, String name, String institution, AccountType type, BigDecimal initialBalance,
                   boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.institution = institution;
        this.type = type;
        this.initialBalance = initialBalance;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account newAccount(String name, String institution, AccountType type, BigDecimal initialBalance) {
        LocalDateTime now = LocalDateTime.now();
        return new Account(null, name, institution, type, initialBalance, true, now, now);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
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
