package com.hfinance.core.config;

import java.nio.file.Path;

public class DatabaseConfig {
    private final Path databasePath;

    public DatabaseConfig() {
        this(AppConfig.dataDirectory().resolve("hfinance.db"));
    }

    public DatabaseConfig(Path databasePath) {
        this.databasePath = databasePath;
    }

    public Path databasePath() {
        return databasePath;
    }

    public String jdbcUrl() {
        return "jdbc:sqlite:" + databasePath.toAbsolutePath();
    }
}
