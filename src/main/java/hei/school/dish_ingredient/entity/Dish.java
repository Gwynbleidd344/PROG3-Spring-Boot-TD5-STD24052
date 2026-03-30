package hei.school.dish_ingredient.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hei.school.dish_ingredient.entity.enums.DishTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;
    private List<DishIngredient> ingredients = new ArrayList<>();

    public Dish(int id, String name, DishTypeEnum dishType, Double sellingPrice) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.sellingPrice = sellingPrice;
        this.ingredients = new ArrayList<>();
    }

    @JsonIgnore
    public Double getDishCost() {
        if (ingredients == null)
            return 0.0;
        return ingredients.stream()
                .mapToDouble(di -> di.getIngredient().getPrice() * di.getQuantityRequired())
                .sum();
    }

    @JsonIgnore
    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException(
                    "Cannot compute gross margin for dish '" + name + "': selling price is null.");
        }
        return sellingPrice - getDishCost();
    }
}