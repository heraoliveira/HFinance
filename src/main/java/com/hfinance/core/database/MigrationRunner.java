package com.hfinance.core.database;

import org.flywaydb.core.Flyway;

public class MigrationRunner {
    private final ConnectionFactory connectionFactory;

    public MigrationRunner(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void migrate() {
        Flyway.configure()
                .dataSource(connectionFactory.jdbcUrl(), "", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}
