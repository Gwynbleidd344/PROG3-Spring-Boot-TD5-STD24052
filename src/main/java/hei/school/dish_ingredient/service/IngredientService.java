package hei.school.dish_ingredient.service;

import hei.school.dish_ingredient.entity.enums.CategoryEnum;
import hei.school.dish_ingredient.entity.Ingredient;
import hei.school.dish_ingredient.entity.StockMovement;
import hei.school.dish_ingredient.entity.StockMovementCreate;
import hei.school.dish_ingredient.entity.StockValue;
import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;
import hei.school.dish_ingredient.exception.BadRequestException;
import hei.school.dish_ingredient.exception.NotFoundException;
import hei.school.dish_ingredient.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> getIngredients(String name, String category,
                                           String dishName, int page, int size) {
        CategoryEnum categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = CategoryEnum.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Unknown category: " + category);
            }
        }
        return ingredientRepository.findByCriteria(name, categoryEnum, dishName, page, size);
    }

    public Ingredient getIngredientById(int id) {
        try {
            return ingredientRepository.findById(id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Ingredient.id=" + id + " is not found");
        }
    }

    public StockValue getStockValueAt(int id, String at, String unit) {
        if (at == null || at.isBlank() || unit == null || unit.isBlank()) {
            throw new BadRequestException(
                    "Either mandatory query parameter `at` or `unit` is not provided.");
        }

        UnitTypeEnum unitEnum;
        try {
            unitEnum = UnitTypeEnum.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Unknown unit: " + unit + ". Valid values: PCS, KG, L");
        }

        Instant instant;
        try {
            instant = Instant.parse(at);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid date format for `at`, e.g. 2024-01-06T12:00:00Z");
        }

        Ingredient ingredient = getIngredientById(id);
        StockValue stockValue = ingredient.getStockValueAt(instant);
        return new StockValue(stockValue.getQuantity(), unitEnum);
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        return ingredientRepository.createIngredients(newIngredients);
    }

    public Ingredient saveIngredient(Ingredient ingredient) {
        return ingredientRepository.saveIngredient(ingredient);
    }
        public List<StockMovement> getStockMovements(int id, String from, String to) {
        getIngredientById(id);
        return ingredientRepository.findStockMovements(
                id, parseInstant(from, "from"), parseInstant(to, "to"));
    }
    
    public List<StockMovement> createStockMovements(int id, List<StockMovementCreate> toCreate) {
        if (toCreate == null || toCreate.isEmpty()) {
            throw new BadRequestException("Request body is mandatory and must not be empty.");
        }
        getIngredientById(id);
        return ingredientRepository.createStockMovements(id, toCreate);
    }

    private Instant parseInstant(String value, String paramName) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeException e) {
            throw new BadRequestException(
                    "Invalid date format for `" + paramName + "`. " +
                    ", e.g. 2024-01-05T08:00:00Z");
        }
    }
}