package com.fatema.procurement.dto;

import com.fatema.procurement.entity.ReceiptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDTO {

    private Long id;
    private String receiptNumber;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDate receiptDate;
    private String receivedBy;
    private BigDecimal totalAmount;
    private ReceiptStatus status;
    private String statusDisplay;
    private String notes;
    private List<ReceiptItemDTO> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private Integer orderedQuantity;
        private Integer receivedQuantity;
        private BigDecimal costPrice;
        private BigDecimal totalCost;
        private String notes;
    }
}
