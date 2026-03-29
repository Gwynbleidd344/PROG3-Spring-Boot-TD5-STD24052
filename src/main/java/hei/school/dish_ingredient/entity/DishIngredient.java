package hei.school.dish_ingredient.entity;

import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class DishIngredient {
    private int          id;
    private int          idDish;
    private Ingredient   ingredient;
    private double       quantityRequired;
    private UnitTypeEnum unit;
}