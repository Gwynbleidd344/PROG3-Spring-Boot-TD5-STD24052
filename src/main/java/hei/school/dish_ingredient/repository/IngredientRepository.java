package hei.school.dish_ingredient.repository;

import hei.school.dish_ingredient.entity.Ingredient;
import hei.school.dish_ingredient.entity.StockMovement;
import hei.school.dish_ingredient.entity.StockValue;
import hei.school.dish_ingredient.entity.enums.CategoryEnum;
import hei.school.dish_ingredient.entity.enums.MovementTypeEnum;
import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ingredient findById(int id) {
        String sql = "SELECT id, name, price, category FROM ingredient WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Ingredient.id=" + id + " is not found");
            }
            Ingredient ingredient = mapIngredient(rs);
            ingredient.setStockMovementList(findMovementsByIngredientId(conn, id));
            return ingredient;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findAll(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT id, name, price, category FROM ingredient ORDER BY id LIMIT ? OFFSET ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();

            List<Ingredient> list = new ArrayList<>();
            while (rs.next()) {
                Ingredient ingredient = mapIngredient(rs);
                ingredient.setStockMovementList(findMovementsByIngredientId(conn, ingredient.getId()));
                list.add(ingredient);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findByCriteria(String ingredientName, CategoryEnum category,
                                           String dishName, int page, int size) {
        int offset = (page - 1) * size;
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT i.id, i.name, i.price, i.category " +
                "FROM ingredient i " +
                "LEFT JOIN dish_ingredient di ON di.id_ingredient = i.id " +
                "LEFT JOIN dish d ON d.id = di.id_dish " +
                "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (ingredientName != null) {
            sql.append("AND i.name ILIKE ? ");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append("AND i.category = ?::ingredient_category ");
            params.add(category.name());
        }
        if (dishName != null) {
            sql.append("AND d.name ILIKE ? ");
            params.add("%" + dishName + "%");
        }
        sql.append("ORDER BY i.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            List<Ingredient> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapIngredient(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Ingredient> created = new ArrayList<>();
                for (Ingredient ingredient : newIngredients) {
                    if (existsByName(conn, ingredient.getName())) {
                        conn.rollback();
                        throw new RuntimeException(
                                "Ingredient '" + ingredient.getName() + "' already exists. Operation cancelled.");
                    }
                    created.add(insertIngredient(conn, ingredient));
                }
                conn.commit();
                return created;
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient saveIngredient(Ingredient toSave) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Ingredient saved;
                if (toSave.getId() > 0 && existsById(conn, toSave.getId())) {
                    saved = updateIngredient(conn, toSave);
                } else {
                    saved = insertIngredient(conn, toSave);
                }
                if (toSave.getStockMovementList() != null) {
                    for (StockMovement movement : toSave.getStockMovementList()) {
                        insertStockMovement(conn, saved.getId(), movement);
                    }
                }
                conn.commit();
                saved.setStockMovementList(findMovementsByIngredientId(conn, saved.getId()));
                return saved;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean existsByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM ingredient WHERE name ILIKE ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private boolean existsById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private Ingredient insertIngredient(Connection conn, Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredient (name, price, category) " +
                     "VALUES (?, ?, ?::ingredient_category) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ingredient.getName());
            ps.setDouble(2, ingredient.getPrice());
            ps.setString(3, ingredient.getCategory().name());
            ResultSet rs = ps.executeQuery();
            rs.next();
            ingredient.setId(rs.getInt("id"));
            return ingredient;
        }
    }

    private Ingredient updateIngredient(Connection conn, Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredient SET name = ?, price = ?, category = ?::ingredient_category " +
                     "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ingredient.getName());
            ps.setDouble(2, ingredient.getPrice());
            ps.setString(3, ingredient.getCategory().name());
            ps.setInt(4, ingredient.getId());
            ps.executeUpdate();
            return ingredient;
        }
    }

    private void insertStockMovement(Connection conn, int ingredientId, StockMovement movement) throws SQLException {
        String sql = """
                INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
                VALUES (?, ?, ?, ?::mouvement_type, ?::unit_type, ?)
                ON CONFLICT (id) DO NOTHING
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (movement.getId() > 0) {
                ps.setInt(1, movement.getId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, ingredientId);
            ps.setDouble(3, movement.getValue().getQuantity());
            ps.setString(4, movement.getType().name());
            ps.setString(5, movement.getValue().getUnit().name());
            ps.setTimestamp(6, Timestamp.from(movement.getCreationDatetime()));
            ps.executeUpdate();
        }
    }

    List<StockMovement> findMovementsByIngredientId(Connection conn,
                                                    int ingredientId) throws SQLException {
        String sql = "SELECT id, quantity, type, unit, creation_datetime " +
                     "FROM stock_movement WHERE id_ingredient = ? ORDER BY creation_datetime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();
            List<StockMovement> list = new ArrayList<>();
            while (rs.next()) {
                StockValue sv = new StockValue(
                        rs.getDouble("quantity"),
                        UnitTypeEnum.valueOf(rs.getString("unit"))
                );
                StockMovement sm = new StockMovement(
                        rs.getInt("id"),
                        sv,
                        MovementTypeEnum.valueOf(rs.getString("type")),
                        rs.getTimestamp("creation_datetime").toInstant()
                );
                list.add(sm);
            }
            return list;
        }
    }

    Ingredient mapIngredient(ResultSet rs) throws SQLException {
        return new Ingredient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                CategoryEnum.valueOf(rs.getString("category"))
        );
    }
}