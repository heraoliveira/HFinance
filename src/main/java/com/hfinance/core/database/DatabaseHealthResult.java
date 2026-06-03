package com.hfinance.core.database;

public record DatabaseHealthResult(boolean healthy, String integrityStatus, String schemaVersion) {
    public static DatabaseHealthResult healthy(String integrityStatus, String schemaVersion) {
        return new DatabaseHealthResult(true, integrityStatus, schemaVersion);
    }

    public static DatabaseHealthResult unhealthy(String integrityStatus) {
        return new DatabaseHealthResult(false, integrityStatus, "");
    }
}
