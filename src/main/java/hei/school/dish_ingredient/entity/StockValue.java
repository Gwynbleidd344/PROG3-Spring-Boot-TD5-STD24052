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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class StockValue {
    private double      quantity;
    private UnitTypeEnum unit;
}
