package com.fatema.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity{
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String country;

    @Column(length = 50)
    private String contactPerson;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 20)
    private String taxId;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}
