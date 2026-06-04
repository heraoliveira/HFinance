package com.hfinance.infrastructure.repository.jdbc;

import com.hfinance.core.database.ConnectionFactory;
import com.hfinance.core.exception.RepositoryException;
import com.hfinance.domain.enums.CategoryType;
import com.hfinance.domain.model.Category;
import com.hfinance.infrastructure.repository.CategoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcCategoryRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Category save(Category category) {
        String sql = """
                INSERT INTO categories
                (name, type, color, is_default, is_active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType().name());
            statement.setString(3, category.getColor());
            statement.setInt(4, category.isDefaultCategory() ? 1 : 0);
            statement.setInt(5, category.isActive() ? 1 : 0);
            statement.setString(6, category.getCreatedAt().toString());
            statement.setString(7, category.getUpdatedAt().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getLong(1)).orElse(category);
                }
            }
            return category;
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível salvar a categoria.", ex);
        }
    }

    @Override
    public void update(Category category) {
        String sql = """
                UPDATE categories
                   SET name = ?, type = ?, color = ?, is_active = ?, updated_at = ?
                 WHERE id = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getType().name());
            statement.setString(3, category.getColor());
            statement.setInt(4, category.isActive() ? 1 : 0);
            statement.setString(5, LocalDateTime.now().toString());
            statement.setLong(6, category.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível atualizar a categoria.", ex);
        }
    }

    @Override
    public void deactivate(Long id) {
        updateActive(id, false);
    }

    @Override
    public void reactivate(Long id) {
        updateActive(id, true);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível excluir a categoria.", ex);
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível buscar a categoria.", ex);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY is_active DESC, type DESC, name ASC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return readList(resultSet);
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as categorias.", ex);
        }
    }

    @Override
    public List<Category> findByType(CategoryType type) {
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY is_active DESC, name ASC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return readList(resultSet);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as categorias.", ex);
        }
    }

    @Override
    public List<Category> findActiveByType(CategoryType type) {
        String sql = "SELECT * FROM categories WHERE type = ? AND is_active = 1 ORDER BY name ASC";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return readList(resultSet);
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível listar as categorias ativas.", ex);
        }
    }

    @Override
    public boolean existsActiveByNameAndType(String name, CategoryType type, Long ignoredId) {
        String sql = """
                SELECT 1
                  FROM categories
                 WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))
                   AND type = ?
                   AND is_active = 1
                   AND (? IS NULL OR id <> ?)
                 LIMIT 1
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, type.name());
            if (ignoredId == null) {
                statement.setObject(3, null);
                statement.setObject(4, null);
            } else {
                statement.setLong(3, ignoredId);
                statement.setLong(4, ignoredId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível verificar duplicidade da categoria.", ex);
        }
    }

    @Override
    public boolean hasTransactions(Long id) {
        return hasReferences("transactions", "category_id", id);
    }

    @Override
    public boolean hasBudgets(Long id) {
        return hasReferences("budgets", "category_id", id);
    }

    private void updateActive(Long id, boolean active) {
        String sql = "UPDATE categories SET is_active = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, active ? 1 : 0);
            statement.setString(2, LocalDateTime.now().toString());
            statement.setLong(3, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível atualizar o status da categoria.", ex);
        }
    }

    private boolean hasReferences(String table, String column, Long id) {
        String sql = "SELECT 1 FROM " + table + " WHERE " + column + " = ? LIMIT 1";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Não foi possível verificar uso da categoria.", ex);
        }
    }

    private List<Category> readList(ResultSet resultSet) throws SQLException {
        List<Category> categories = new ArrayList<>();
        while (resultSet.next()) {
            categories.add(map(resultSet));
        }
        return categories;
    }

    private Category map(ResultSet resultSet) throws SQLException {
        LocalDateTime createdAt = JdbcSupport.getLocalDateTime(resultSet, "created_at");
        LocalDateTime updatedAt = JdbcSupport.getLocalDateTime(resultSet, "updated_at");
        return new Category(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                CategoryType.valueOf(resultSet.getString("type")),
                resultSet.getString("color"),
                resultSet.getInt("is_default") == 1,
                resultSet.getInt("is_active") == 1,
                createdAt,
                updatedAt == null ? createdAt : updatedAt
        );
    }
}
