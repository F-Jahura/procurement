package com.fatema.procurement.service;

import com.fatema.procurement.dto.OrderItemDTO;
import com.fatema.procurement.entity.*;
import com.fatema.procurement.repository.OrderItemRepository;
import com.fatema.procurement.repository.ProductRepository;
import com.fatema.procurement.repository.PurchaseOrderRepository;
import com.fatema.procurement.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    private String generateOrderNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = purchaseOrderRepository.count() + 1;
        return "PO-" + year + "-" + String.format("%04d", count);
    }

    // Создать заказ
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        // Проверяем поставщика
        if (order.getSupplier() != null && order.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(order.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        // Если дата заказа не указана — ставим сегодня
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        // Если статус не указан — ставим DRAFT
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.DRAFT);
        }

        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder createPurchaseOrderWithItems(PurchaseOrder order, List<OrderItemDTO> items) {
        // 1. Генерируем номер заказа
        order.setOrderNumber(generateOrderNumber());

        // 2. Устанавливаем дату заказа
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        // 3. Если статус не указан — DRAFT
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.DRAFT);
        }

        // 4. Проверяем поставщика
        if (order.getSupplier() != null && order.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(order.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        // 5. Сохраняем заказ
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        // 6. Добавляем товары в заказ и обновляем остатки
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : items) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Проверяем остаток
            if (product.getCurrentStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            // Создаём OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItemRepository.save(orderItem);

            // Обновляем остаток товара
            product.setCurrentStock(product.getCurrentStock() - itemDTO.getQuantity());
            productRepository.save(product);

            // Считаем общую сумму
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        // 7. Обновляем сумму заказа
        savedOrder.setTotalAmount(totalAmount);
        return purchaseOrderRepository.save(savedOrder);
    }


    // Получить все заказы
    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAll();
    }

    // Получить заказ по ID
    public Optional<PurchaseOrder> getOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    // Получить заказы по номеру (содержит)
    public List<PurchaseOrder> getOrdersByNumber(String orderNumber) {
        return purchaseOrderRepository.findByOrderNumberContaining(orderNumber);
    }

    // Получить заказы по статусу
    public List<PurchaseOrder> getOrdersByStatus(OrderStatus status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    // Получить заказы по дате (диапазон)
    public List<PurchaseOrder> getOrdersByDateRange(LocalDate start, LocalDate end) {
        return purchaseOrderRepository.findByOrderDateBetween(start, end);
    }

    // Получить заказы по поставщику
    public List<PurchaseOrder> getOrdersBySupplier(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId);
    }

    // Получить заказы, которые должны быть доставлены в ближайшие 7 дней
    public List<PurchaseOrder> getOrdersDeliveringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return purchaseOrderRepository.findByDeliveryDateBetween(today, nextWeek);
    }

    // Обновить заказ
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrder orderDetails) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setOrderNumber(orderDetails.getOrderNumber());
        order.setOrderDate(orderDetails.getOrderDate());
        order.setDeliveryDate(orderDetails.getDeliveryDate());
        order.setTotalAmount(orderDetails.getTotalAmount());
        order.setStatus(orderDetails.getStatus());
        order.setNotes(orderDetails.getNotes());

        if (orderDetails.getSupplier() != null && orderDetails.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(orderDetails.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        return purchaseOrderRepository.save(order);
    }


    // Обновить статус заказа
    public PurchaseOrder updateOrderStatus(Long id, OrderStatus newStatus) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(newStatus);
        return purchaseOrderRepository.save(order);
    }

    // Удалить заказ
    public void deletePurchaseOrder(Long id) {
        purchaseOrderRepository.deleteById(id);
    }

}
