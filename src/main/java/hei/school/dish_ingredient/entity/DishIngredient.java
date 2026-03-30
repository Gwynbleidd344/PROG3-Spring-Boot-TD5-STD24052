package hei.school.dish_ingredient.entity;

import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishIngredient {
    private int          id;
    private int          idDish;
    private Ingredient   ingredient;
    private double       quantityRequired;
    private UnitTypeEnum unit;
}