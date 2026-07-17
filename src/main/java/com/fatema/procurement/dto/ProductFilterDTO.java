package com.fatema.procurement.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductFilterDTO {
    private String name;
    private String sku;
    private Long supplierId;
    private Integer minStock;
    private Boolean lowStockOnly;
    private Integer salesPeriodMonths;  // ← новое поле: период продаж (1-6)

    public ProductFilterDTO(String name, Long supplierId, Integer minStock, Boolean lowStockOnly) {
        this.name = name;
        this.supplierId = supplierId;
        this.minStock = minStock;
        this.lowStockOnly = lowStockOnly;
        this.salesPeriodMonths = 3; // значение по умолчанию
    }

    // Геттеры и сеттеры

    public boolean isEmpty() {
        return (name == null || name.trim().isEmpty()) &&
                supplierId == null &&
                minStock == null &&
                lowStockOnly == null &&
                salesPeriodMonths == null;
    }
}
