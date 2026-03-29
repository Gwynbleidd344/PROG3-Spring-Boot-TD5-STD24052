package hei.school.dish_ingredient.entity;

import hei.school.dish_ingredient.entity.enums.CategoryEnum;
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
public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
}
