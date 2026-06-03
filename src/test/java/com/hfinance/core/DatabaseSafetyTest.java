package com.hfinance.core;

import com.hfinance.core.backup.BackupRetentionPolicy;
import com.hfinance.core.backup.BackupService;
import com.hfinance.core.backup.BackupType;
import com.hfinance.core.config.AppConfig;
import com.hfinance.core.config.AppPaths;
import com.hfinance.core.config.DatabaseConfig;
import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.database.DatabaseHealthChecker;
import com.hfinance.core.database.DatabaseHealthResult;
import com.hfinance.core.database.DatabaseInitializer;
import com.hfinance.core.database.MigrationRunner;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSafetyTest {
    @TempDir
    Path tempDir;

    @Test
    void appPathsPreferAppDataOnWindowsAndUserHomeFallbackElsewhere() {
        AppPaths paths = AppPaths.createDefault();
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isBlank()) {
            assertThat(paths.dataDirectory()).isEqualTo(Path.of(appData, AppConfig.APP_NAME));
        } else {
            assertThat(paths.dataDirectory()).isEqualTo(Path.of(System.getProperty("user.home"), ".hfinance"));
        }
        assertThat(paths.databasePath().getFileName().toString()).isEqualTo("hfinance.db");
        assertThat(paths.backupsDirectory().getFileName().toString()).isEqualTo("backups");
        assertThat(paths.logsDirectory().getFileName().toString()).isEqualTo("logs");
        assertThat(paths.exportsDirectory().getFileName().toString()).isEqualTo("exports");
    }

    @Test
    void createsAndValidatesManualBackup() {
        Path database = tempDir.resolve("database").resolve("hfinance.db");
        new DatabaseInitializer(new DatabaseConfig(database)).initialize();

        DatabaseHealthChecker healthChecker = new DatabaseHealthChecker();
        BackupService backupService = new BackupService(tempDir.resolve("backups"), healthChecker,
                BackupRetentionPolicy.defaultPolicy());

        Path backup = backupService.createBackup(database, BackupType.MANUAL).backupPath();

        assertThat(backup).exists();
        assertThat(healthChecker.check(backup).healthy()).isTrue();
        assertThat(backupService.backupCount()).isEqualTo(1);
    }

    @Test
    void invalidDatabaseIsNotHealthy() throws IOException {
        Path database = tempDir.resolve("invalid.db");
        Files.writeString(database, "isto não é um banco sqlite válido");

        DatabaseHealthResult result = new DatabaseHealthChecker().check(database);

        assertThat(result.healthy()).isFalse();
        assertThat(result.integrityStatus()).isNotBlank();
    }

    @Test
    void createsAutomaticBackupBeforePendingMigration() throws IOException {
        Path database = tempDir.resolve("migration").resolve("hfinance.db");
        Files.createDirectories(database.getParent());
        ConnectionFactory connectionFactory = new ConnectionFactory(database);
        Flyway.configure()
                .dataSource(connectionFactory.jdbcUrl(), "", "")
                .locations("classpath:db/migration")
                .target("1")
                .load()
                .migrate();

        DatabaseHealthChecker healthChecker = new DatabaseHealthChecker();
        BackupService backupService = new BackupService(tempDir.resolve("migration-backups"), healthChecker,
                BackupRetentionPolicy.defaultPolicy());

        new MigrationRunner(connectionFactory, database, backupService, true).migrate();

        assertThat(countDatabaseFiles(tempDir.resolve("migration-backups"))).isEqualTo(1);
        assertThat(healthChecker.check(database).schemaVersion()).isEqualTo("2");
    }

    @Test
    void migratesLegacyDatabaseOnlyAfterBackupAndKeepsOriginalFile() throws IOException {
        Path legacyDatabase = tempDir.resolve("legacy").resolve("hfinance.db");
        new DatabaseInitializer(new DatabaseConfig(legacyDatabase)).initialize();

        Path officialRoot = tempDir.resolve("official");
        AppPaths paths = new AppPaths(
                officialRoot,
                officialRoot.resolve("hfinance.db"),
                officialRoot.resolve("backups"),
                officialRoot.resolve("logs"),
                officialRoot.resolve("exports"),
                officialRoot.resolve("config.properties"),
                legacyDatabase,
                true
        );

        new DatabaseInitializer(new DatabaseConfig(paths)).initialize();

        assertThat(paths.databasePath()).exists();
        assertThat(legacyDatabase).exists();
        assertThat(countDatabaseFiles(paths.backupsDirectory())).isEqualTo(1);
        assertThat(new DatabaseHealthChecker().check(paths.databasePath()).healthy()).isTrue();
    }

    private long countDatabaseFiles(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".db")).count();
        }
    }
}
