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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcCategoryRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
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
        String sql = "SELECT * FROM categories ORDER BY type DESC, name ASC";
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
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY name ASC";
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

    private List<Category> readList(ResultSet resultSet) throws SQLException {
        List<Category> categories = new ArrayList<>();
        while (resultSet.next()) {
            categories.add(map(resultSet));
        }
        return categories;
    }

    private Category map(ResultSet resultSet) throws SQLException {
        return new Category(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                CategoryType.valueOf(resultSet.getString("type")),
                resultSet.getString("color"),
                resultSet.getInt("is_default") == 1,
                JdbcSupport.getLocalDateTime(resultSet, "created_at")
        );
    }
}
