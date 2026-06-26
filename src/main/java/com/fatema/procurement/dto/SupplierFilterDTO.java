package com.fatema.procurement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierFilterDTO {
    private String name;
    private String country;
    private Boolean active;

    public boolean isEmpty() {
        return (name == null || name.trim().isEmpty()) &&
                (country == null || country.trim().isEmpty()) &&
                active == null;
    }
}
