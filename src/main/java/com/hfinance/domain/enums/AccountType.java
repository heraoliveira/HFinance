package com.hfinance.domain.enums;

public enum AccountType {
    CHECKING_ACCOUNT("Conta corrente"),
    SAVINGS_ACCOUNT("Conta poupança"),
    WALLET("Carteira"),
    CASH("Dinheiro físico"),
    INVESTMENT("Investimento"),
    OTHER("Outros");

    private final String displayName;

    AccountType(String displayName) {
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
