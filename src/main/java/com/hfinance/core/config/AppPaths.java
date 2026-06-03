package com.hfinance.core.config;

import java.nio.file.Path;

public record AppPaths(
        Path dataDirectory,
        Path databasePath,
        Path backupsDirectory,
        Path logsDirectory,
        Path exportsDirectory,
        Path configPath,
        Path legacyDatabasePath,
        boolean legacyMigrationEnabled
) {
    public static AppPaths createDefault() {
        Path dataDirectory = officialDataDirectory();
        return new AppPaths(
                dataDirectory,
                dataDirectory.resolve("hfinance.db"),
                dataDirectory.resolve("backups"),
                dataDirectory.resolve("logs"),
                dataDirectory.resolve("exports"),
                dataDirectory.resolve("config.properties"),
                Path.of("data", "hfinance.db"),
                true
        );
    }

    public static AppPaths forDatabasePath(Path databasePath) {
        Path dataDirectory = databasePath.toAbsolutePath().getParent();
        return new AppPaths(
                dataDirectory,
                databasePath,
                dataDirectory.resolve("backups"),
                dataDirectory.resolve("logs"),
                dataDirectory.resolve("exports"),
                dataDirectory.resolve("config.properties"),
                Path.of("data", "hfinance.db"),
                false
        );
    }

    private static Path officialDataDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, AppConfig.APP_NAME);
        }
        return Path.of(System.getProperty("user.home"), ".hfinance");
    }
}
