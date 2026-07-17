package com.fatema.procurement.dto;

import com.fatema.procurement.entity.Product;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductWithSalesDTO {
    private Product product;
    private int salesCount;          // количество продаж за период
    private int stockStatus;         // 0=норма, 1=мало, 2=критично

    public ProductWithSalesDTO(Product product, int salesCount) {
        this.product = product;
        this.salesCount = salesCount;
        this.stockStatus = calculateStockStatus(product.getCurrentStock());
    }

    private int calculateStockStatus(Integer stock) {
        if (stock == null) return 0;
        if (stock < 10) return 2;      // критично
        if (stock < 30) return 1;      // мало
        return 0;                       // норма
    }

    // Методы для шаблона
    public String getStockStatusText() {
        switch (stockStatus) {
            case 2: return "Критично!";
            case 1: return "Мало";
            default: return "Норма";
        }
    }

    public String getStockStatusColor() {
        switch (stockStatus) {
            case 2: return "bg-danger";
            case 1: return "bg-warning text-dark";
            default: return "bg-success";
        }
    }

    public Long getId() { return product.getId(); }
    public String getName() { return product.getName(); }
    public String getSku() { return product.getSku(); }
    public String getSupplierName() {
        return product.getSupplier() != null ? product.getSupplier().getName() : "-";
    }
    public BigDecimal getCostPrice() { return product.getCostPrice(); }
    public Integer getCurrentStock() { return product.getCurrentStock(); }
    public Integer getMinStock() { return product.getMinStock(); }
    public String getUnit() { return product.getUnit(); }
    public Long getSupplierId() {
        return product.getSupplier() != null ? product.getSupplier().getId() : null;
    }
}
