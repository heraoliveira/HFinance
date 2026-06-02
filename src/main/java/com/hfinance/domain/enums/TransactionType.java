package com.hfinance.domain.enums;

public enum TransactionType {
    INCOME("Receita"),
    EXPENSE("Despesa");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
