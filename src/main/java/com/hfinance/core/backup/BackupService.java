package com.hfinance.core.backup;

import com.hfinance.core.database.DatabaseHealthChecker;
import com.hfinance.core.logging.AppLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackupService {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final Logger LOGGER = AppLogger.logger(BackupService.class);

    private final Path backupsDirectory;
    private final DatabaseHealthChecker healthChecker;
    private final BackupRetentionPolicy retentionPolicy;

    public BackupService(Path backupsDirectory, DatabaseHealthChecker healthChecker,
                         BackupRetentionPolicy retentionPolicy) {
        this.backupsDirectory = backupsDirectory;
        this.healthChecker = healthChecker;
        this.retentionPolicy = retentionPolicy;
    }

    public BackupResult createBackup(Path databasePath, BackupType type) {
        if (!Files.exists(databasePath)) {
            throw new IllegalStateException("Não há banco de dados para backup.");
        }
        try {
            Files.createDirectories(backupsDirectory);
            LocalDateTime createdAt = LocalDateTime.now();
            Path backupPath = nextBackupPath(createdAt);
            createConsistentCopy(databasePath, backupPath);
            healthChecker.requireHealthy(backupPath);
            writeMetadata(backupPath, type, createdAt);
            if (type == BackupType.AUTOMATIC) {
                applyAutomaticRetention(backupPath);
            }
            LOGGER.info(() -> "Backup " + type + " criado em " + backupPath.toAbsolutePath());
            return new BackupResult(backupPath, type, createdAt);
        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Falha ao criar backup do banco " + databasePath.toAbsolutePath(), ex);
            throw new IllegalStateException("Não foi possível criar o backup.", ex);
        }
    }

    public Path backupsDirectory() {
        return backupsDirectory;
    }

    public Path lastBackup() {
        try (Stream<Path> backups = Files.list(backupsDirectory)) {
            return backups
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .max(Comparator.comparing(path -> path.toFile().lastModified()))
                    .orElse(null);
        } catch (IOException ex) {
            return null;
        }
    }

    public long backupCount() {
        try (Stream<Path> backups = Files.list(backupsDirectory)) {
            return backups
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .count();
        } catch (IOException ex) {
            return 0;
        }
    }

    private void createConsistentCopy(Path databasePath, Path backupPath) throws SQLException, IOException {
        String target = backupPath.toAbsolutePath().toString().replace("'", "''");
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            statement.execute("VACUUM INTO '" + target + "'");
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "VACUUM INTO falhou; usando cópia de arquivo como fallback.", ex);
            Files.copy(databasePath, backupPath);
        }
    }

    private Path nextBackupPath(LocalDateTime createdAt) throws IOException {
        String baseName = "hfinance-backup-" + FILE_TIMESTAMP.format(createdAt);
        Path candidate = backupsDirectory.resolve(baseName + ".db");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = backupsDirectory.resolve(baseName + "-%03d.db".formatted(suffix));
            suffix++;
        }
        return candidate;
    }

    private void writeMetadata(Path backupPath, BackupType type, LocalDateTime createdAt) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("type", type.name());
        properties.setProperty("createdAt", createdAt.toString());
        try (var writer = Files.newBufferedWriter(metadataPath(backupPath))) {
            properties.store(writer, "HFinance backup metadata");
        }
    }

    private Path metadataPath(Path backupPath) {
        String fileName = backupPath.getFileName().toString().replaceFirst("\\.db$", ".properties");
        return backupPath.getParent().resolve(fileName);
    }

    private void applyAutomaticRetention(Path newestBackup) {
        try (Stream<Path> backups = Files.list(backupsDirectory)) {
            List<Path> automaticBackups = backups
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .filter(path -> !path.equals(newestBackup))
                    .filter(this::isAutomaticBackup)
                    .sorted(Comparator.comparing((Path path) -> path.toFile().lastModified()).reversed())
                    .toList();
            for (int index = retentionPolicy.maxAutomaticBackups() - 1; index < automaticBackups.size(); index++) {
                Path backup = automaticBackups.get(index);
                Files.deleteIfExists(backup);
                Files.deleteIfExists(metadataPath(backup));
                LOGGER.info(() -> "Backup automático antigo removido: " + backup.toAbsolutePath());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Não foi possível limpar backups automáticos antigos.", ex);
        }
    }

    private boolean isAutomaticBackup(Path backupPath) {
        Path metadata = metadataPath(backupPath);
        if (!Files.exists(metadata)) {
            return false;
        }
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(metadata)) {
            properties.load(reader);
            return BackupType.AUTOMATIC.name().equals(properties.getProperty("type"));
        } catch (IOException ex) {
            return false;
        }
    }
}
