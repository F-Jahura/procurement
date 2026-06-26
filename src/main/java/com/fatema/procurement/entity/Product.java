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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "current_stock")
    private Integer currentStock;

    // ⚠️ УДАЛИТЕ ЭТО ПОЛЕ (если оно есть)
    // @Column(name = "sales_last_3_months")
    // private Integer salesLast3Months;

    @Column(length = 50)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Transient
    private Long supplierId;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (currentStock == null) {
            currentStock = 0;
        }
    }
}
