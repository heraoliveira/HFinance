package com.hfinance.core.config;

import java.nio.file.Path;

public class DatabaseConfig {
    private final AppPaths appPaths;
    private final Path databasePath;

    public DatabaseConfig() {
        this(AppPaths.createDefault());
    }

    public DatabaseConfig(Path databasePath) {
        this(AppPaths.forDatabasePath(databasePath));
    }

    public DatabaseConfig(AppPaths appPaths) {
        this.appPaths = appPaths;
        this.databasePath = appPaths.databasePath();
    }

    public AppPaths appPaths() {
        return appPaths;
    }

    public boolean legacyMigrationEnabled() {
        return appPaths.legacyMigrationEnabled();
    }

    public Path legacyDatabasePath() {
        return appPaths.legacyDatabasePath();
    }

    public Path backupsDirectory() {
        return appPaths.backupsDirectory();
    }

    public Path logsDirectory() {
        return appPaths.logsDirectory();
    }

    public Path exportsDirectory() {
        return appPaths.exportsDirectory();
    }

    public Path configPath() {
        return appPaths.configPath();
    }

    public Path databasePath() {
        return databasePath;
    }

    public String jdbcUrl() {
        return "jdbc:sqlite:" + databasePath.toAbsolutePath();
    }
}
