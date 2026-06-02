package com.hfinance.core.database;

import com.hfinance.core.exception.RepositoryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
    private final String jdbcUrl;

    public ConnectionFactory(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            enableForeignKeys(connection);
            return connection;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível abrir conexão com o banco de dados.", ex);
        }
    }

    private void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }
}
