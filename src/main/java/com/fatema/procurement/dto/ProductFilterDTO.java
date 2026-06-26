package com.fatema.procurement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductFilterDTO {
    private String name;
    private Long supplierId;
    private Integer minStock;
    private Boolean lowStockOnly;
    private Integer salesPeriodMonths;  // ← новое поле: период продаж (1-6)

    // Геттеры и сеттеры

    public boolean isEmpty() {
        return (name == null || name.trim().isEmpty()) &&
                supplierId == null &&
                minStock == null &&
                lowStockOnly == null &&
                salesPeriodMonths == null;
    }
}
