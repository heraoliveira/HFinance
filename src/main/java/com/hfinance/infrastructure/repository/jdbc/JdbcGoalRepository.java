package com.hfinance.infrastructure.repository.jdbc;

import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.exception.RepositoryException;
import com.hfinance.domain.enums.GoalStatus;
import com.hfinance.domain.model.Goal;
import com.hfinance.infrastructure.repository.GoalRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGoalRepository implements GoalRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcGoalRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Goal save(Goal goal) {
        String sql = """
                INSERT INTO goals (account_id, name, target_amount, current_amount, deadline, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindGoal(statement, goal);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    goal.setId(keys.getLong(1));
                }
            }
            return goal;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível salvar a meta.", ex);
        }
    }

    @Override
    public void update(Goal goal) {
        String sql = """
                UPDATE goals
                   SET account_id = ?, name = ?, target_amount = ?, current_amount = ?, deadline = ?,
                       status = ?, updated_at = ?
                 WHERE id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (goal.getAccountId() == null) {
                statement.setObject(1, null);
            } else {
                statement.setLong(1, goal.getAccountId());
            }
            statement.setString(2, goal.getName().trim());
            statement.setBigDecimal(3, goal.getTargetAmount());
            statement.setBigDecimal(4, goal.getCurrentAmount());
            statement.setString(5, goal.getDeadline().toString());
            statement.setString(6, goal.getStatus().name());
            statement.setString(7, goal.getUpdatedAt().toString());
            statement.setLong(8, goal.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível atualizar a meta.", ex);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível excluir a meta.", ex);
        }
    }

    @Override
    public Optional<Goal> findById(Long id) {
        String sql = "SELECT * FROM goals WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível buscar a meta.", ex);
        }
    }

    @Override
    public List<Goal> findAll() {
        String sql = "SELECT * FROM goals ORDER BY deadline ASC, name ASC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Goal> goals = new ArrayList<>();
            while (resultSet.next()) {
                goals.add(map(resultSet));
            }
            return goals;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as metas.", ex);
        }
    }

    private void bindGoal(PreparedStatement statement, Goal goal) throws SQLException {
        if (goal.getAccountId() == null) {
            statement.setObject(1, null);
        } else {
            statement.setLong(1, goal.getAccountId());
        }
        statement.setString(2, goal.getName().trim());
        statement.setBigDecimal(3, goal.getTargetAmount());
        statement.setBigDecimal(4, goal.getCurrentAmount());
        statement.setString(5, goal.getDeadline().toString());
        statement.setString(6, goal.getStatus().name());
        statement.setString(7, goal.getCreatedAt().toString());
        statement.setString(8, goal.getUpdatedAt().toString());
    }

    private Goal map(ResultSet resultSet) throws SQLException {
        long accountId = resultSet.getLong("account_id");
        boolean accountWasNull = resultSet.wasNull();
        return new Goal(
                resultSet.getLong("id"),
                accountWasNull ? null : accountId,
                resultSet.getString("name"),
                JdbcSupport.getBigDecimal(resultSet, "target_amount"),
                JdbcSupport.getBigDecimal(resultSet, "current_amount"),
                LocalDate.parse(resultSet.getString("deadline")),
                GoalStatus.valueOf(resultSet.getString("status")),
                JdbcSupport.getLocalDateTime(resultSet, "created_at"),
                JdbcSupport.getLocalDateTime(resultSet, "updated_at")
        );
    }
}
