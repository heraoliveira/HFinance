package com.hfinance.infrastructure.repository.jdbc;

import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.exception.RepositoryException;
import com.hfinance.domain.model.Budget;
import com.hfinance.infrastructure.repository.BudgetRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBudgetRepository implements BudgetRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcBudgetRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Budget save(Budget budget) {
        String sql = """
                INSERT INTO budgets (category_id, name, month, year, limit_amount, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, budget.getCategoryId());
            statement.setString(2, budget.getName().trim());
            statement.setInt(3, budget.getMonth());
            statement.setInt(4, budget.getYear());
            statement.setBigDecimal(5, budget.getLimitAmount());
            statement.setString(6, budget.getCreatedAt().toString());
            statement.setString(7, budget.getUpdatedAt().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    budget.setId(keys.getLong(1));
                }
            }
            return budget;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível salvar o orçamento.", ex);
        }
    }

    @Override
    public void update(Budget budget) {
        String sql = """
                UPDATE budgets
                   SET category_id = ?, name = ?, month = ?, year = ?, limit_amount = ?, updated_at = ?
                 WHERE id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, budget.getCategoryId());
            statement.setString(2, budget.getName().trim());
            statement.setInt(3, budget.getMonth());
            statement.setInt(4, budget.getYear());
            statement.setBigDecimal(5, budget.getLimitAmount());
            statement.setString(6, budget.getUpdatedAt().toString());
            statement.setLong(7, budget.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível atualizar o orçamento.", ex);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível excluir o orçamento.", ex);
        }
    }

    @Override
    public Optional<Budget> findById(Long id) {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível buscar o orçamento.", ex);
        }
    }

    @Override
    public List<Budget> findAll() {
        String sql = "SELECT * FROM budgets ORDER BY year DESC, month DESC, name ASC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Budget> budgets = new ArrayList<>();
            while (resultSet.next()) {
                budgets.add(map(resultSet));
            }
            return budgets;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar os orçamentos.", ex);
        }
    }

    @Override
    public boolean existsByCategoryMonthYear(Long categoryId, int month, int year, Long excludedId) {
        String sql = """
                SELECT COUNT(1)
                  FROM budgets
                 WHERE category_id = ?
                   AND month = ?
                   AND year = ?
                   AND (? IS NULL OR id <> ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, categoryId);
            statement.setInt(2, month);
            statement.setInt(3, year);
            if (excludedId == null) {
                statement.setObject(4, null);
                statement.setObject(5, null);
            } else {
                statement.setLong(4, excludedId);
                statement.setLong(5, excludedId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível verificar duplicidade do orçamento.", ex);
        }
    }

    private Budget map(ResultSet resultSet) throws SQLException {
        return new Budget(
                resultSet.getLong("id"),
                resultSet.getLong("category_id"),
                resultSet.getString("name"),
                resultSet.getInt("month"),
                resultSet.getInt("year"),
                JdbcSupport.getBigDecimal(resultSet, "limit_amount"),
                JdbcSupport.getLocalDateTime(resultSet, "created_at"),
                JdbcSupport.getLocalDateTime(resultSet, "updated_at")
        );
    }
}
