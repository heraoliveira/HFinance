package com.hfinance.core.backup;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record BackupResult(Path backupPath, BackupType type, LocalDateTime createdAt) {
    public String fileName() {
        return backupPath.getFileName().toString();
    }
}
