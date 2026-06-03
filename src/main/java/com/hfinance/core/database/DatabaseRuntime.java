package com.hfinance.core.database;

import com.hfinance.core.backup.BackupService;
import com.hfinance.core.config.AppPaths;

public record DatabaseRuntime(
        ConnectionFactory connectionFactory,
        AppPaths appPaths,
        DatabaseHealthChecker healthChecker,
        BackupService backupService
) {
}
