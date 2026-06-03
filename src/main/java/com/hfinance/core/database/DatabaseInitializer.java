package com.hfinance.core.database;

import com.hfinance.core.backup.BackupRetentionPolicy;
import com.hfinance.core.backup.BackupService;
import com.hfinance.core.backup.BackupType;
import com.hfinance.core.config.AppPaths;
import com.hfinance.core.config.AppVersion;
import com.hfinance.core.config.DatabaseConfig;
import com.hfinance.core.logging.AppLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Logger;

public class DatabaseInitializer {
    private static final Logger LOGGER = AppLogger.logger(DatabaseInitializer.class);

    private final DatabaseConfig databaseConfig;

    public DatabaseInitializer(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public ConnectionFactory initialize() {
        return initializeRuntime().connectionFactory();
    }

    public DatabaseRuntime initializeRuntime() {
        AppPaths appPaths = databaseConfig.appPaths();
        try {
            createDirectories(appPaths);
            AppLogger.configure(appPaths.logsDirectory());
            ensureConfigFile(appPaths);
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível preparar a pasta de dados.", ex);
        }

        DatabaseHealthChecker healthChecker = new DatabaseHealthChecker();
        BackupService backupService = new BackupService(appPaths.backupsDirectory(), healthChecker,
                BackupRetentionPolicy.defaultPolicy());

        Path databasePath = resolveDatabasePath(appPaths, healthChecker, backupService);
        boolean existingDatabase = isExistingDatabase(databasePath);
        if (existingDatabase) {
            healthChecker.requireHealthy(databasePath);
        }

        ConnectionFactory connectionFactory = new ConnectionFactory(databasePath);
        new MigrationRunner(connectionFactory, databasePath, backupService, existingDatabase).migrate();
        if (Files.exists(databasePath)) {
            healthChecker.requireHealthy(databasePath);
        }
        return new DatabaseRuntime(connectionFactory, appPaths, healthChecker, backupService);
    }

    private void createDirectories(AppPaths appPaths) throws IOException {
        Files.createDirectories(appPaths.dataDirectory());
        Files.createDirectories(appPaths.backupsDirectory());
        Files.createDirectories(appPaths.logsDirectory());
        Files.createDirectories(appPaths.exportsDirectory());
    }

    private void ensureConfigFile(AppPaths appPaths) throws IOException {
        if (Files.exists(appPaths.configPath())) {
            return;
        }
        Properties properties = new Properties();
        properties.setProperty("app.version", AppVersion.VERSION);
        properties.setProperty("database.path", appPaths.databasePath().toAbsolutePath().toString());
        properties.setProperty("backup.retention.maxAutomaticBackups", "10");
        properties.setProperty("diagnostics.includeLogTail", "true");
        try (var writer = Files.newBufferedWriter(appPaths.configPath())) {
            properties.store(writer, "HFinance configuration");
        }
    }

    private Path resolveDatabasePath(AppPaths appPaths, DatabaseHealthChecker healthChecker,
                                     BackupService backupService) {
        Path officialDatabase = appPaths.databasePath();
        Path legacyDatabase = appPaths.legacyDatabasePath();
        boolean officialExists = Files.exists(officialDatabase);
        boolean legacyExists = appPaths.legacyMigrationEnabled() && Files.exists(legacyDatabase);

        if (legacyExists && !officialExists) {
            healthChecker.requireHealthy(legacyDatabase);
            backupService.createBackup(legacyDatabase, BackupType.AUTOMATIC);
            try {
                Files.copy(legacyDatabase, officialDatabase, StandardCopyOption.COPY_ATTRIBUTES);
                healthChecker.requireHealthy(officialDatabase);
                LOGGER.info(() -> "Banco legado copiado para o diretório oficial. Legado="
                        + legacyDatabase.toAbsolutePath() + ", oficial=" + officialDatabase.toAbsolutePath());
            } catch (IOException ex) {
                throw new IllegalStateException("Não foi possível migrar o banco legado com segurança.", ex);
            }
        } else if (legacyExists) {
            LOGGER.info(() -> "Banco legado também encontrado e preservado: " + legacyDatabase.toAbsolutePath());
        }

        return officialDatabase;
    }

    private boolean isExistingDatabase(Path databasePath) {
        try {
            return Files.exists(databasePath) && Files.size(databasePath) > 0;
        } catch (IOException ex) {
            return Files.exists(databasePath);
        }
    }
}
