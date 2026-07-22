package com.fatema.procurement.service;

import com.fatema.procurement.entity.*;
import com.fatema.procurement.repository.GoodsReceiptRepository;
import com.fatema.procurement.repository.ReceiptItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoodsReceiptService {

    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    public List<GoodsReceipt> getAllReceipts() {
        return goodsReceiptRepository.findAll();
    }

    public GoodsReceipt getReceiptById(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Приходная накладная не найдена"));
    }

    @Transactional
    public GoodsReceipt createFromOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        // Проверяем, можно ли создать приход
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Заказ должен быть отгружен для создания приходной накладной");
        }

        // Проверяем, не создан ли уже приход для этого заказа
        List<GoodsReceipt> existingReceipts = goodsReceiptRepository.findByPurchaseOrderId(orderId);
        if (!existingReceipts.isEmpty()) {
            throw new RuntimeException("Для этого заказа уже создана приходная накладная");
        }

        // Создаём приходную накладную
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setReceiptNumber(generateReceiptNumber());
        receipt.setPurchaseOrder(order);
        receipt.setSupplier(order.getSupplier());
        receipt.setReceiptDate(LocalDate.now());
        receipt.setReceivedBy(getCurrentUser());
        receipt.setStatus(ReceiptStatus.DRAFT);

        // Копируем позиции из заказа
        for (OrderItem orderItem : order.getItems()) {
            ReceiptItem receiptItem = new ReceiptItem();
            receiptItem.setGoodsReceipt(receipt);
            receiptItem.setProduct(orderItem.getProduct());
            receiptItem.setOrderedQuantity(orderItem.getQuantity());
            receiptItem.setReceivedQuantity(orderItem.getQuantity()); // По умолчанию = заказано
            receiptItem.setCostPrice(orderItem.getProduct().getCostPrice());
            receiptItem.calculateTotalCost();
            receipt.addItem(receiptItem);
        }

        receipt.recalculateTotal();
        return goodsReceiptRepository.save(receipt);
    }

    @Transactional
    public GoodsReceipt confirmReceipt(Long id, List<ReceiptItemUpdateDTO> updates) {
        GoodsReceipt receipt = getReceiptById(id);

        if (receipt.getStatus() == ReceiptStatus.CONFIRMED) {
            throw new RuntimeException("Приход уже подтверждён");
        }

        if (receipt.getStatus() == ReceiptStatus.CANCELLED) {
            throw new RuntimeException("Приход отменён");
        }

        // Обновляем фактические количества
        for (ReceiptItemUpdateDTO update : updates) {
            ReceiptItem item = receipt.getItems().stream()
                    .filter(i -> i.getId().equals(update.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Позиция не найдена"));

            if (update.getReceivedQuantity() != null && update.getReceivedQuantity() > 0) {
                item.setReceivedQuantity(update.getReceivedQuantity());
            }
            if (update.getCostPrice() != null && update.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                item.setCostPrice(update.getCostPrice());
            }
            item.calculateTotalCost();
        }

        receipt.recalculateTotal();

        // Проверяем, полностью ли получен заказ
        boolean isFull = receipt.getItems().stream()
                .allMatch(i -> i.getReceivedQuantity().equals(i.getOrderedQuantity()));

        receipt.setStatus(isFull ? ReceiptStatus.CONFIRMED : ReceiptStatus.PARTIAL);

        // Увеличиваем остатки товаров
        for (ReceiptItem item : receipt.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getCurrentStock() + item.getReceivedQuantity();
            product.setCurrentStock(newStock);
            // Используем метод updateStock для обновления только остатка
            productService.updateStock(product.getId(), newStock);
        }

        // Обновляем статус заказа
        PurchaseOrder order = receipt.getPurchaseOrder();
        order.setStatus(OrderStatus.DELIVERED);
        purchaseOrderService.updatePurchaseOrder(order.getId(), order);

        return goodsReceiptRepository.save(receipt);
    }

    @Transactional
    public GoodsReceipt cancelReceipt(Long id) {
        GoodsReceipt receipt = getReceiptById(id);

        if (receipt.getStatus() == ReceiptStatus.CONFIRMED) {
            // Возвращаем остатки
            for (ReceiptItem item : receipt.getItems()) {
                Product product = item.getProduct();
                int newStock = product.getCurrentStock() - item.getReceivedQuantity();
                if (newStock < 0) {
                    throw new RuntimeException("Недостаточно товара на складе для отмены прихода");
                }
                product.setCurrentStock(newStock);
                // Используем метод updateStock для обновления только остатка
                productService.updateStock(product.getId(), newStock);
            }
        }

        receipt.setStatus(ReceiptStatus.CANCELLED);
        return goodsReceiptRepository.save(receipt);
    }

    public List<GoodsReceipt> getReceiptsByStatus(ReceiptStatus status) {
        return goodsReceiptRepository.findByStatus(status);
    }

    public List<GoodsReceipt> getReceiptsByDateRange(LocalDate startDate, LocalDate endDate) {
        return goodsReceiptRepository.findByReceiptDateBetween(startDate, endDate);
    }

    public long countByStatus(ReceiptStatus status) {
        return goodsReceiptRepository.countByStatus(status);
    }

    public long countByDate(LocalDate date) {
        return goodsReceiptRepository.countByDate(date);
    }

    public BigDecimal getTotalForMonth() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        return goodsReceiptRepository.sumTotalAmountBetweenDates(startDate, endDate);
    }

    private String generateReceiptNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = goodsReceiptRepository.count() + 1;
        return String.format("ПН-%s-%05d", datePart, count);
    }

    private String getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return username != null ? username : "system";
    }

    // Внутренний класс для обновления позиций
    public static class ReceiptItemUpdateDTO {
        private Long itemId;
        private Integer receivedQuantity;
        private BigDecimal costPrice;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public Integer getReceivedQuantity() { return receivedQuantity; }
        public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
        public BigDecimal getCostPrice() { return costPrice; }
        public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    }
}
