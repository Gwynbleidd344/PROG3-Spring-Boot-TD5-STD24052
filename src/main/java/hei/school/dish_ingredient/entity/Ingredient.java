package hei.school.dish_ingredient.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import hei.school.dish_ingredient.entity.enums.CategoryEnum;
import hei.school.dish_ingredient.entity.enums.MovementTypeEnum;
import hei.school.dish_ingredient.entity.enums.UnitTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    private int                 id;
    private String              name;
    private Double              price;
    private CategoryEnum        category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient(int id, String name, Double price, CategoryEnum category) {
        this.id                = id;
        this.name              = name;
        this.price             = price;
        this.category          = category;
        this.stockMovementList = new ArrayList<>();
    }
    public StockValue getStockValueAt(Instant t) {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return new StockValue(0.0, UnitTypeEnum.KG);
        }

        double total = 0.0;
        for (StockMovement m : stockMovementList) {
            if (m.getCreationDatetime() != null && !m.getCreationDatetime().isAfter(t)) {
                if (m.getType() == MovementTypeEnum.IN) {
                    total += m.getValue().getQuantity();
                } else {
                    total -= m.getValue().getQuantity();
                }
            }
        }

        UnitTypeEnum unit = stockMovementList.stream()
                .filter(m -> m.getValue() != null)
                .map(m -> m.getValue().getUnit())
                .findFirst()
                .orElse(UnitTypeEnum.KG);

        return new StockValue(total, unit);
    }
}