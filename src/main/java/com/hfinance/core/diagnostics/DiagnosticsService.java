package com.hfinance.core.diagnostics;

import com.hfinance.core.backup.BackupService;
import com.hfinance.core.config.AppPaths;
import com.hfinance.core.config.AppVersion;
import com.hfinance.core.database.DatabaseHealthChecker;
import com.hfinance.core.database.DatabaseHealthResult;
import com.hfinance.core.logging.AppLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiagnosticsService {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final Logger LOGGER = AppLogger.logger(DiagnosticsService.class);

    private final AppPaths appPaths;
    private final DatabaseHealthChecker healthChecker;
    private final BackupService backupService;

    public DiagnosticsService(AppPaths appPaths, DatabaseHealthChecker healthChecker, BackupService backupService) {
        this.appPaths = appPaths;
        this.healthChecker = healthChecker;
        this.backupService = backupService;
    }

    public Path exportDiagnostic() {
        try {
            Files.createDirectories(appPaths.exportsDirectory());
            Path file = appPaths.exportsDirectory().resolve("hfinance-diagnostico-"
                    + FILE_TIMESTAMP.format(LocalDateTime.now()) + ".txt");
            Files.writeString(file, diagnosticText());
            LOGGER.info(() -> "Diagnóstico exportado em " + file.toAbsolutePath());
            return file;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Falha ao exportar diagnóstico.", ex);
            throw new IllegalStateException("Não foi possível exportar o diagnóstico.", ex);
        }
    }

    public String diagnosticText() {
        DatabaseHealthResult health = Files.exists(appPaths.databasePath())
                ? healthChecker.check(appPaths.databasePath())
                : DatabaseHealthResult.unhealthy("Arquivo inexistente");
        Path lastBackup = backupService.lastBackup();
        return """
                HFinance
                Versão: %s
                Data/hora: %s
                Sistema operacional: %s
                Java Runtime: %s
                Diretório de dados: %s
                Banco de dados: %s
                Banco existe: %s
                Integridade do banco: %s
                Versão do schema: %s
                Diretório de backups: %s
                Último backup: %s
                Quantidade de backups: %d
                Diretório de logs: %s
                Diretório de exports: %s

                Últimas linhas do log:
                %s

                Observação: dados financeiros sensíveis não são exportados intencionalmente.
                """.formatted(
                AppVersion.VERSION,
                LocalDateTime.now(),
                System.getProperty("os.name"),
                System.getProperty("java.version"),
                appPaths.dataDirectory().toAbsolutePath(),
                appPaths.databasePath().toAbsolutePath(),
                Files.exists(appPaths.databasePath()) ? "sim" : "não",
                health.integrityStatus(),
                health.schemaVersion(),
                appPaths.backupsDirectory().toAbsolutePath(),
                lastBackup == null ? "nenhum backup encontrado" : lastBackup.getFileName(),
                backupService.backupCount(),
                appPaths.logsDirectory().toAbsolutePath(),
                appPaths.exportsDirectory().toAbsolutePath(),
                safeLogTail()
        );
    }

    private String safeLogTail() {
        Path log = appPaths.logsDirectory().resolve("hfinance.log");
        if (!Files.exists(log)) {
            return "Log ainda não criado.";
        }
        try {
            List<String> lines = Files.readAllLines(log);
            int from = Math.max(0, lines.size() - 25);
            return String.join(System.lineSeparator(), lines.subList(from, lines.size()));
        } catch (IOException ex) {
            return String.join(System.lineSeparator(), Collections.singletonList("Não foi possível ler o log."));
        }
    }
}
