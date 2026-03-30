package hei.school.dish_ingredient.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

import hei.school.dish_ingredient.entity.enums.MovementTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    private int              id;
    private StockValue       value;
    private MovementTypeEnum type;
    private Instant          creationDatetime;
}
