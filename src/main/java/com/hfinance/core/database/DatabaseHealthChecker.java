package com.hfinance.core.database;

import com.hfinance.core.logging.AppLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseHealthChecker {
    private static final Logger LOGGER = AppLogger.logger(DatabaseHealthChecker.class);

    public DatabaseHealthResult check(Path databasePath) {
        if (!Files.exists(databasePath)) {
            return DatabaseHealthResult.unhealthy("Arquivo inexistente");
        }
        if (!Files.isReadable(databasePath)) {
            return DatabaseHealthResult.unhealthy("Arquivo sem permissão de leitura");
        }
        String jdbcUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
        try (var connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("PRAGMA integrity_check")) {
                String status = resultSet.next() ? resultSet.getString(1) : "sem resultado";
                if (!"ok".equalsIgnoreCase(status)) {
                    return DatabaseHealthResult.unhealthy(status);
                }
            }
            String schemaVersion = schemaVersion(statement);
            LOGGER.info(() -> "Banco validado com sucesso. Caminho=" + databasePath.toAbsolutePath()
                    + ", schema=" + schemaVersion);
            return DatabaseHealthResult.healthy("ok", schemaVersion);
        } catch (SQLException ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Falha ao validar banco " + databasePath.toAbsolutePath(), ex);
            return DatabaseHealthResult.unhealthy(ex.getMessage());
        }
    }

    public void requireHealthy(Path databasePath) {
        DatabaseHealthResult result = check(databasePath);
        if (!result.healthy()) {
            throw new IllegalStateException("O banco de dados parece estar corrompido ou inacessível: "
                    + result.integrityStatus());
        }
    }

    private String schemaVersion(Statement statement) {
        try (ResultSet exists = statement.executeQuery("""
                SELECT name FROM sqlite_master
                WHERE type = 'table' AND name = 'flyway_schema_history'
                """)) {
            if (!exists.next()) {
                return "sem migrations";
            }
        } catch (SQLException ex) {
            return "indisponível";
        }

        try (ResultSet version = statement.executeQuery("""
                SELECT COALESCE(MAX(version), 'baseline') FROM flyway_schema_history WHERE success = 1
                """)) {
            return version.next() ? version.getString(1) : "sem versão";
        } catch (SQLException ex) {
            return "indisponível";
        }
    }
}
