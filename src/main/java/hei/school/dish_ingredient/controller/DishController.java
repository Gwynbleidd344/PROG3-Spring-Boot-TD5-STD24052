package hei.school.dish_ingredient.controller;

import hei.school.dish_ingredient.entity.Dish;
import hei.school.dish_ingredient.entity.Ingredient;
import hei.school.dish_ingredient.service.DishService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
@CrossOrigin(origins = "*")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes() {
        return ResponseEntity.ok(dishService.getAllDishes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable int id) {
        return ResponseEntity.ok(dishService.getDishById(id));
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<Dish> updateDishIngredients(
            @PathVariable int id,
            @RequestBody(required = false) List<Ingredient> ingredients
    ) {
        return ResponseEntity.ok(dishService.updateDishIngredients(id, ingredients));
    }
}