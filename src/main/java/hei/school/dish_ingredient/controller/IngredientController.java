package hei.school.dish_ingredient.controller;

import hei.school.dish_ingredient.entity.Ingredient;
import hei.school.dish_ingredient.entity.StockMovement;
import hei.school.dish_ingredient.entity.StockMovementCreate;
import hei.school.dish_ingredient.entity.StockValue;
import hei.school.dish_ingredient.service.IngredientService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@CrossOrigin(origins = "*")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> getIngredients(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String name,
            @RequestParam(required = false)    String category,
            @RequestParam(required = false)    String dishName
    ) {
        return ResponseEntity.ok(
                ingredientService.getIngredients(name, category, dishName, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable int id) {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<StockValue> getIngredientStock(
            @PathVariable int id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit
    ) {
        return ResponseEntity.ok(ingredientService.getStockValueAt(id, at, unit));
    }

    @GetMapping("/{id}/stockMovements")
    public ResponseEntity<List<StockMovement>> getIngredientStockMovements(
        @PathVariable int id,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to
    ) {
        return ResponseEntity.ok(ingredientService.getStockMovements(id, from, to));
    }

    @PostMapping("/{id}/stockMovements")
    public ResponseEntity<List<StockMovement>> createStockMovements(
            @PathVariable int id,
            @RequestBody(required = false) List<StockMovementCreate> toCreate
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ingredientService.createStockMovements(id, toCreate));
    }
}