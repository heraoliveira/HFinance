package com.hfinance.domain.enums;

public enum PaymentMethod {
    PIX("Pix"),
    CASH("À vista"),
    DEBIT_CARD("Cartão de débito"),
    CREDIT_CARD("Cartão de crédito"),
    BANK_TRANSFER("Transferência bancária"),
    BANK_SLIP("Boleto"),
    OTHER("Outros");

    private final String displayName;

    PaymentMethod(String displayName) {
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
