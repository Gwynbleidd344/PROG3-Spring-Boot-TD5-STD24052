package hei.school.dish_ingredient.repository;

import hei.school.dish_ingredient.entity.*;
import hei.school.dish_ingredient.entity.enums.CategoryEnum;
import hei.school.dish_ingredient.entity.enums.DishTypeEnum;
import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;
    private final IngredientRepository ingredientRepository;

    public DishRepository(DataSource dataSource, IngredientRepository ingredientRepository) {
        this.dataSource = dataSource;
        this.ingredientRepository = ingredientRepository;
    }

    public Dish findById(int id) {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Dish.id=" + id + " is not found");
            }
            Dish dish = mapDish(rs);
            dish.setIngredients(findDishIngredients(conn, id));
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findAll() {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish ORDER BY id";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            List<Dish> list = new ArrayList<>();
            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setIngredients(findDishIngredients(conn, dish.getId()));
                list.add(dish);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findByIngredientName(String ingredientName) {
        String sql = """
                SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price
                FROM dish d
                JOIN dish_ingredient di ON di.id_dish = d.id
                JOIN ingredient i ON i.id = di.id_ingredient
                WHERE i.name ILIKE ?
                ORDER BY d.id
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();
            List<Dish> list = new ArrayList<>();
            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setIngredients(findDishIngredients(conn, dish.getId()));
                list.add(dish);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Dish saved;
                if (dishToSave.getId() > 0 && existsById(conn, dishToSave.getId())) {
                    saved = updateDish(conn, dishToSave);
                } else {
                    saved = insertDish(conn, dishToSave);
                }
                deleteDishIngredients(conn, saved.getId());
                if (dishToSave.getIngredients() != null) {
                    for (DishIngredient di : dishToSave.getIngredients()) {
                        insertDishIngredient(conn, saved.getId(), di);
                    }
                }
                conn.commit();
                saved.setIngredients(findDishIngredients(conn, saved.getId()));
                return saved;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish updateDishIngredients(int dishId, List<Ingredient> ingredients) {
        Dish dish = findById(dishId);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                deleteDishIngredients(conn, dishId);
                for (Ingredient ing : ingredients) {
                    if (!ingredientExistsById(conn, ing.getId()))
                        continue;
                    DishIngredient di = new DishIngredient(0, dishId, ing, 1.0, UnitTypeEnum.KG);
                    insertDishIngredient(conn, dishId, di);
                }
                conn.commit();
                dish.setIngredients(findDishIngredients(conn, dishId));
                return dish;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean existsById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM dish WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private boolean ingredientExistsById(Connection conn, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private Dish insertDish(Connection conn, Dish dish) throws SQLException {
        String sql = "INSERT INTO dish (name, dish_type, selling_price) " +
                "VALUES (?, ?::dish_type, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dish.getName());
            ps.setString(2, dish.getDishType().name());
            if (dish.getSellingPrice() != null) {
                ps.setDouble(3, dish.getSellingPrice());
            } else {
                ps.setNull(3, Types.NUMERIC);
            }
            ResultSet rs = ps.executeQuery();
            rs.next();
            dish.setId(rs.getInt("id"));
            return dish;
        }
    }

    private Dish updateDish(Connection conn, Dish dish) throws SQLException {
        String sql = "UPDATE dish SET name = ?, dish_type = ?::dish_type, selling_price = ? " +
                "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dish.getName());
            ps.setString(2, dish.getDishType().name());
            if (dish.getSellingPrice() != null) {
                ps.setDouble(3, dish.getSellingPrice());
            } else {
                ps.setNull(3, Types.NUMERIC);
            }
            ps.setInt(4, dish.getId());
            ps.executeUpdate();
            return dish;
        }
    }

    private void deleteDishIngredients(Connection conn, int dishId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private void insertDishIngredient(Connection conn, int dishId,
            DishIngredient di) throws SQLException {
        String sql = "INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) " +
                "VALUES (?, ?, ?, ?::unit_type) ON CONFLICT (id_dish, id_ingredient) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ps.setInt(2, di.getIngredient().getId());
            ps.setDouble(3, di.getQuantityRequired());
            ps.setString(4, di.getUnit().name());
            ps.executeUpdate();
        }
    }

    private List<DishIngredient> findDishIngredients(Connection conn,
            int dishId) throws SQLException {
        String sql = """
                SELECT di.id, di.quantity_required, di.unit,
                       i.id AS ing_id, i.name AS ing_name, i.price, i.category
                FROM dish_ingredient di
                JOIN ingredient i ON i.id = di.id_ingredient
                WHERE di.id_dish = ?
                ORDER BY di.id
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();
            List<DishIngredient> list = new ArrayList<>();
            while (rs.next()) {
                Ingredient ing = new Ingredient(
                        rs.getInt("ing_id"),
                        rs.getString("ing_name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")));
                DishIngredient di = new DishIngredient(
                        rs.getInt("id"),
                        dishId,
                        ing,
                        rs.getDouble("quantity_required"),
                        UnitTypeEnum.valueOf(rs.getString("unit")));
                list.add(di);
            }
            return list;
        }
    }

    private Dish mapDish(ResultSet rs) throws SQLException {
        double sp = rs.getDouble("selling_price");
        Double sellingPrice = rs.wasNull() ? null : sp;
        return new Dish(
                rs.getInt("id"),
                rs.getString("name"),
                DishTypeEnum.valueOf(rs.getString("dish_type")),
                sellingPrice);
    }
}