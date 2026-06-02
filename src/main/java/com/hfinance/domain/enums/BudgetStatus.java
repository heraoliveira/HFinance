package com.hfinance.domain.enums;

public enum BudgetStatus {
    ON_TRACK("Dentro do limite"),
    WARNING("Atenção"),
    EXCEEDED("Excedido");

    private final String displayName;

    BudgetStatus(String displayName) {
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
