package com.hfinance.infrastructure.repository.jdbc;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class JdbcSupport {
    private JdbcSupport() {
    }

    static BigDecimal getBigDecimal(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    static LocalDateTime getLocalDateTime(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : LocalDateTime.parse(value.replace(' ', 'T'));
    }

    static Integer getInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    static void bind(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value instanceof LocalDate date) {
            statement.setString(index, date.toString());
        } else if (value instanceof BigDecimal decimal) {
            statement.setBigDecimal(index, decimal);
        } else if (value instanceof Enum<?> enumValue) {
            statement.setString(index, enumValue.name());
        } else {
            statement.setObject(index, value);
        }
    }
}
