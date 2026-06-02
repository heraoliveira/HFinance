package com.hfinance.domain.enums;

public enum GoalStatus {
    IN_PROGRESS("Em andamento"),
    COMPLETED("Concluída"),
    OVERDUE("Atrasada");

    private final String displayName;

    GoalStatus(String displayName) {
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
