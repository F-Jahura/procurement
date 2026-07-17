package com.fatema.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String sku;

    @Column(length = 500)
    private String description;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;  // Себестоимость

    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;  // Продажная цена

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(length = 50)
    private String unit;

    @Column(name = "sales_last_3_months")
    private Integer salesLast3Months = 0;  // Продажи за 3 месяца

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Transient
    private Long supplierId;

    // Метод для расчёта продажной цены
    public void calculateSellingPrice() {
        if (costPrice != null) {
            this.sellingPrice = costPrice.multiply(BigDecimal.valueOf(2));  // +100%
        }
    }

    // Метод для увеличения продаж
    public void addSales(int quantity) {
        if (this.salesLast3Months == null) {
            this.salesLast3Months = 0;
        }
        this.salesLast3Months += quantity;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        super.onCreate();
        if (currentStock == null) {
            currentStock = 0;
        }
        if (salesLast3Months == null) {
            salesLast3Months = 0;
        }
        calculateSellingPrice();
    }
}