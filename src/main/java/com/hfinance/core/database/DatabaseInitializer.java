package com.hfinance.core.database;

import com.hfinance.core.config.DatabaseConfig;

import java.io.IOException;
import java.nio.file.Files;

public class DatabaseInitializer {
    private final DatabaseConfig databaseConfig;

    public DatabaseInitializer(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public ConnectionFactory initialize() {
        try {
            Files.createDirectories(databaseConfig.databasePath().getParent());
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível preparar a pasta de dados.", ex);
        }
        ConnectionFactory connectionFactory = new ConnectionFactory(databaseConfig.jdbcUrl());
        new MigrationRunner(connectionFactory).migrate();
        return connectionFactory;
    }
}
