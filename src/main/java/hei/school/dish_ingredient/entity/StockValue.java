package hei.school.dish_ingredient.entity;

import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockValue {
    private double      quantity;
    private UnitTypeEnum unit;
}