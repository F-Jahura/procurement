package com.fatema.procurement.dto;

import java.math.BigDecimal;

public class DashboardStats {
    private long suppliersCount;
    private long productsCount;
    private long activeOrdersCount;
    private BigDecimal totalOrdersAmount;

    public DashboardStats(long suppliersCount, long productsCount, long activeOrdersCount, BigDecimal totalOrdersAmount) {
        this.suppliersCount = suppliersCount;
        this.productsCount = productsCount;
        this.activeOrdersCount = activeOrdersCount;
        this.totalOrdersAmount = totalOrdersAmount;
    }

    // Геттеры
    public long getSuppliersCount() { return suppliersCount; }
    public long getProductsCount() { return productsCount; }
    public long getActiveOrdersCount() { return activeOrdersCount; }
    public BigDecimal getTotalOrdersAmount() { return totalOrdersAmount; }
}
