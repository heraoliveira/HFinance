package com.hfinance.infrastructure.repository.jdbc;

import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.exception.RepositoryException;
import com.hfinance.domain.enums.AccountType;
import com.hfinance.domain.model.Account;
import com.hfinance.infrastructure.repository.AccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcAccountRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Account save(Account account) {
        String sql = """
                INSERT INTO accounts (name, institution, type, initial_balance, is_active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindAccount(statement, account);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                }
            }
            return account;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível salvar a conta.", ex);
        }
    }

    @Override
    public void update(Account account) {
        String sql = """
                UPDATE accounts
                   SET name = ?, institution = ?, type = ?, initial_balance = ?, is_active = ?, updated_at = ?
                 WHERE id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getName().trim());
            statement.setString(2, account.getInstitution().trim());
            statement.setString(3, account.getType().name());
            statement.setBigDecimal(4, account.getInitialBalance());
            statement.setInt(5, account.isActive() ? 1 : 0);
            statement.setString(6, account.getUpdatedAt().toString());
            statement.setLong(7, account.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível atualizar a conta.", ex);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível buscar a conta.", ex);
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts ORDER BY is_active DESC, name ASC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                accounts.add(map(resultSet));
            }
            return accounts;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as contas.", ex);
        }
    }

    @Override
    public boolean existsActiveByName(String name, Long excludedId) {
        String sql = """
                SELECT COUNT(1)
                  FROM accounts
                 WHERE LOWER(name) = LOWER(?)
                   AND is_active = 1
                   AND (? IS NULL OR id <> ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim());
            if (excludedId == null) {
                statement.setObject(2, null);
                statement.setObject(3, null);
            } else {
                statement.setLong(2, excludedId);
                statement.setLong(3, excludedId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível verificar duplicidade da conta.", ex);
        }
    }

    @Override
    public boolean hasTransactions(Long accountId) {
        String sql = "SELECT COUNT(1) FROM transactions WHERE account_id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível verificar transações da conta.", ex);
        }
    }

    @Override
    public void deactivate(Long accountId) {
        String sql = "UPDATE accounts SET is_active = 0, updated_at = ? WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LocalDateTime.now().toString());
            statement.setLong(2, accountId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível inativar a conta.", ex);
        }
    }

    @Override
    public void delete(Long accountId) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível excluir a conta.", ex);
        }
    }

    private void bindAccount(PreparedStatement statement, Account account) throws SQLException {
        statement.setString(1, account.getName().trim());
        statement.setString(2, account.getInstitution().trim());
        statement.setString(3, account.getType().name());
        statement.setBigDecimal(4, account.getInitialBalance());
        statement.setInt(5, account.isActive() ? 1 : 0);
        statement.setString(6, account.getCreatedAt().toString());
        statement.setString(7, account.getUpdatedAt().toString());
    }

    private Account map(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("institution"),
                AccountType.valueOf(resultSet.getString("type")),
                JdbcSupport.getBigDecimal(resultSet, "initial_balance"),
                resultSet.getInt("is_active") == 1,
                JdbcSupport.getLocalDateTime(resultSet, "created_at"),
                JdbcSupport.getLocalDateTime(resultSet, "updated_at")
        );
    }
}
