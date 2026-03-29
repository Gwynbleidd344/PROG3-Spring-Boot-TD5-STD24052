package hei.school.dish_ingredient.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

import hei.school.dish_ingredient.entity.enums.MovementTypeEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class StockMovement {
    private int              id;
    private StockValue       value;
    private MovementTypeEnum type;
    private Instant          creationDatetime;
}
