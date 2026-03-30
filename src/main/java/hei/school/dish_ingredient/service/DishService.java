package hei.school.dish_ingredient.service;

import hei.school.dish_ingredient.entity.Dish;
import hei.school.dish_ingredient.entity.Ingredient;
import hei.school.dish_ingredient.exception.BadRequestException;
import hei.school.dish_ingredient.exception.NotFoundException;
import hei.school.dish_ingredient.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    public Dish getDishById(int id) {
        try {
            return dishRepository.findById(id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Dish.id=" + id + " is not found");
        }
    }

    public Dish saveDish(Dish dish) {
        return dishRepository.saveDish(dish);
    }

    public List<Dish> findDishesByIngredientName(String ingredientName) {
        return dishRepository.findByIngredientName(ingredientName);
    }

    public Dish updateDishIngredients(int dishId, List<Ingredient> ingredients) {
        if (ingredients == null) {
            throw new BadRequestException(
                    "Request body is mandatory. Please provide a list of ingredients.");
        }
        try {
            dishRepository.findById(dishId);
        } catch (RuntimeException e) {
            throw new NotFoundException("Dish.id=" + dishId + " is not found");
        }
        return dishRepository.updateDishIngredients(dishId, ingredients);
    }
}