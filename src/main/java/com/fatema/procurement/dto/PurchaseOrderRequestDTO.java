package com.fatema.procurement.dto;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderRequestDTO {
    private Long supplierId;
    private List<OrderItemRequestDTO> items;

    @Data
    public static class OrderItemRequestDTO {
        private Long productId;
        private Integer quantity;
    }
}
