package com.hfinance.infrastructure.repository.jdbc;

import com.hfinance.application.dto.TransactionFilterDTO;
import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.exception.RepositoryException;
import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.RecurrenceType;
import com.hfinance.domain.enums.TransactionType;
import com.hfinance.domain.model.Transaction;
import com.hfinance.infrastructure.repository.TransactionRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTransactionRepository implements TransactionRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcTransactionRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Transaction save(Transaction transaction) {
        String sql = """
                INSERT INTO transactions
                (account_id, category_id, transaction_date, transaction_type, payment_method, description, amount,
                 recurrence_group_id, recurrence_type, recurrence_index, recurrence_total, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindTransaction(statement, transaction);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setId(keys.getLong(1));
                }
            }
            return transaction;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível salvar a transação.", ex);
        }
    }

    @Override
    public void update(Transaction transaction) {
        String sql = """
                UPDATE transactions
                   SET account_id = ?, category_id = ?, transaction_date = ?, transaction_type = ?,
                       payment_method = ?, description = ?, amount = ?, recurrence_group_id = ?,
                       recurrence_type = ?, recurrence_index = ?, recurrence_total = ?, updated_at = ?
                 WHERE id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, transaction.getAccountId());
            statement.setLong(2, transaction.getCategoryId());
            statement.setString(3, transaction.getTransactionDate().toString());
            statement.setString(4, transaction.getTransactionType().name());
            statement.setString(5, transaction.getPaymentMethod().name());
            statement.setString(6, transaction.getDescription());
            statement.setBigDecimal(7, transaction.getAmount());
            statement.setString(8, transaction.getRecurrenceGroupId());
            statement.setString(9, transaction.getRecurrenceType() == RecurrenceType.NONE
                    ? null
                    : transaction.getRecurrenceType().name());
            JdbcSupport.bind(statement, 10, transaction.getRecurrenceIndex());
            JdbcSupport.bind(statement, 11, transaction.getRecurrenceTotal());
            statement.setString(12, transaction.getUpdatedAt().toString());
            statement.setLong(13, transaction.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível atualizar a transação.", ex);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível excluir a transação.", ex);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível buscar a transação.", ex);
        }
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC, id DESC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return readList(resultSet);
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as transações.", ex);
        }
    }

    @Override
    public List<Transaction> findLatest(int limit) {
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC, id DESC LIMIT ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                return readList(resultSet);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as últimas transações.", ex);
        }
    }

    @Override
    public List<Transaction> findByFilter(TransactionFilterDTO filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, filter);
        sql.append(" ORDER BY transaction_date DESC, id DESC");

        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return readList(resultSet);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível filtrar as transações.", ex);
        }
    }

    @Override
    public List<Transaction> findRecurringFrom(String recurrenceGroupId, LocalDate startDate) {
        String sql = """
                SELECT *
                  FROM transactions
                 WHERE recurrence_group_id = ?
                   AND transaction_date >= ?
                 ORDER BY transaction_date ASC, id ASC
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, recurrenceGroupId);
            statement.setString(2, startDate.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return readList(resultSet);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as transações recorrentes.", ex);
        }
    }

    @Override
    public BigDecimal calculateAccountMovement(Long accountId) {
        String sql = """
                SELECT COALESCE(SUM(CASE
                    WHEN transaction_type = 'INCOME' THEN amount
                    ELSE -amount
                END), 0) AS movement
                  FROM transactions
                 WHERE account_id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? JdbcSupport.getBigDecimal(resultSet, "movement") : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível calcular o saldo da conta.", ex);
        }
    }

    @Override
    public BigDecimal sumExpensesByCategoryAndMonth(Long categoryId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        String sql = """
                SELECT COALESCE(SUM(amount), 0) AS total
                  FROM transactions
                 WHERE category_id = ?
                   AND transaction_type = 'EXPENSE'
                   AND transaction_date BETWEEN ? AND ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, categoryId);
            statement.setString(2, start.toString());
            statement.setString(3, end.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? JdbcSupport.getBigDecimal(resultSet, "total") : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível calcular o orçamento utilizado.", ex);
        }
    }

    @Override
    public List<Transaction> findBetween(LocalDate startDate, LocalDate endDate) {
        TransactionFilterDTO filter = new TransactionFilterDTO(startDate, endDate, null, null,
                null, null, null, null, null, null, null);
        return findByFilter(filter);
    }

    private void appendFilters(StringBuilder sql, List<Object> parameters, TransactionFilterDTO filter) {
        if (filter == null) {
            return;
        }
        if (filter.effectiveStartDate() != null) {
            sql.append(" AND transaction_date >= ?");
            parameters.add(filter.effectiveStartDate());
        }
        if (filter.effectiveEndDate() != null) {
            sql.append(" AND transaction_date <= ?");
            parameters.add(filter.effectiveEndDate());
        }
        if (filter.accountId() != null) {
            sql.append(" AND account_id = ?");
            parameters.add(filter.accountId());
        }
        if (filter.categoryId() != null) {
            sql.append(" AND category_id = ?");
            parameters.add(filter.categoryId());
        }
        if (filter.transactionType() != null) {
            sql.append(" AND transaction_type = ?");
            parameters.add(filter.transactionType());
        }
        if (filter.paymentMethod() != null) {
            sql.append(" AND payment_method = ?");
            parameters.add(filter.paymentMethod());
        }
        if (filter.description() != null && !filter.description().isBlank()) {
            sql.append(" AND LOWER(description) LIKE LOWER(?)");
            parameters.add("%" + filter.description().trim() + "%");
        }
        if (filter.minAmount() != null) {
            sql.append(" AND amount >= ?");
            parameters.add(filter.minAmount());
        }
        if (filter.maxAmount() != null) {
            sql.append(" AND amount <= ?");
            parameters.add(filter.maxAmount());
        }
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            JdbcSupport.bind(statement, i + 1, parameters.get(i));
        }
    }

    private void bindTransaction(PreparedStatement statement, Transaction transaction) throws SQLException {
        statement.setLong(1, transaction.getAccountId());
        statement.setLong(2, transaction.getCategoryId());
        statement.setString(3, transaction.getTransactionDate().toString());
        statement.setString(4, transaction.getTransactionType().name());
        statement.setString(5, transaction.getPaymentMethod().name());
        statement.setString(6, transaction.getDescription());
        statement.setBigDecimal(7, transaction.getAmount());
        statement.setString(8, transaction.getRecurrenceGroupId());
        statement.setString(9, transaction.getRecurrenceType() == RecurrenceType.NONE
                ? null
                : transaction.getRecurrenceType().name());
        JdbcSupport.bind(statement, 10, transaction.getRecurrenceIndex());
        JdbcSupport.bind(statement, 11, transaction.getRecurrenceTotal());
        statement.setString(12, transaction.getCreatedAt().toString());
        statement.setString(13, transaction.getUpdatedAt().toString());
    }

    private List<Transaction> readList(ResultSet resultSet) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        while (resultSet.next()) {
            transactions.add(map(resultSet));
        }
        return transactions;
    }

    private Transaction map(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getLong("id"),
                resultSet.getLong("account_id"),
                resultSet.getLong("category_id"),
                LocalDate.parse(resultSet.getString("transaction_date")),
                TransactionType.valueOf(resultSet.getString("transaction_type")),
                PaymentMethod.valueOf(resultSet.getString("payment_method")),
                resultSet.getString("description"),
                JdbcSupport.getBigDecimal(resultSet, "amount"),
                resultSet.getString("recurrence_group_id"),
                recurrenceType(resultSet.getString("recurrence_type")),
                JdbcSupport.getInteger(resultSet, "recurrence_index"),
                JdbcSupport.getInteger(resultSet, "recurrence_total"),
                JdbcSupport.getLocalDateTime(resultSet, "created_at"),
                JdbcSupport.getLocalDateTime(resultSet, "updated_at")
        );
    }

    private RecurrenceType recurrenceType(String value) {
        return value == null || value.isBlank() ? RecurrenceType.NONE : RecurrenceType.valueOf(value);
    }
}
