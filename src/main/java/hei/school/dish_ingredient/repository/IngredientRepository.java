package hei.school.dish_ingredient.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import hei.school.dish_ingredient.entity.Ingredient;
import hei.school.dish_ingredient.entity.enums.CategoryEnum;

public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ingredient findAll(int page, int size) {
        int offset = (page - 1) * size;
        String query = """
                    SELECT id, name, price, category
                    FROM ingredient
                    ORDER BY id
                    LIMIT ? OFFSET ?
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                return ingredient;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }

        String query = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            for (Ingredient ingredient : newIngredients) {
                ps.setString(1, ingredient.getName());
                ps.setDouble(2, ingredient.getPrice());
                ps.setString(3, ingredient.getCategory().name());
                ps.addBatch();
            }
            ps.executeBatch();

            ResultSet rs = ps.getGeneratedKeys();

            List<Ingredient> ingredients = newIngredients;
            int index = 0;
            while (rs.next()) {
                ingredients.get(index++).setId(rs.getInt(1));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}