package com.hfinance.core.database;

import com.hfinance.core.backup.BackupService;
import com.hfinance.core.backup.BackupType;
import com.hfinance.core.logging.AppLogger;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MigrationRunner {
    private static final Logger LOGGER = AppLogger.logger(MigrationRunner.class);

    private final ConnectionFactory connectionFactory;
    private final Path databasePath;
    private final BackupService backupService;
    private final boolean backupBeforeMigration;

    public MigrationRunner(ConnectionFactory connectionFactory) {
        this(connectionFactory, connectionFactory.databasePath(), null, false);
    }

    public MigrationRunner(ConnectionFactory connectionFactory, Path databasePath, BackupService backupService,
                           boolean backupBeforeMigration) {
        this.connectionFactory = connectionFactory;
        this.databasePath = databasePath;
        this.backupService = backupService;
        this.backupBeforeMigration = backupBeforeMigration;
    }

    public void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(connectionFactory.jdbcUrl(), "", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        try {
            if (backupBeforeMigration && backupService != null && databasePath != null
                    && flyway.info().pending().length > 0) {
                backupService.createBackup(databasePath, BackupType.AUTOMATIC);
            }
            flyway.migrate();
            LOGGER.info("Migrations Flyway executadas com sucesso.");
        } catch (FlywayException ex) {
            LOGGER.log(Level.SEVERE, "Falha ao executar migrations Flyway.", ex);
            throw new IllegalStateException("""
                    Não foi possível atualizar o banco de dados.

                    Um backup foi criado antes da atualização quando havia migrations pendentes. Se precisar, você pode recuperar seus dados pela pasta de backups.
                    """, ex);
        }
    }
}
