package com.hfinance.core.backup;

public record BackupRetentionPolicy(int maxAutomaticBackups) {
    public static BackupRetentionPolicy defaultPolicy() {
        return new BackupRetentionPolicy(10);
    }
}
