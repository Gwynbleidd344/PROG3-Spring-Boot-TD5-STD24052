package hei.school.dish_ingredient.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}