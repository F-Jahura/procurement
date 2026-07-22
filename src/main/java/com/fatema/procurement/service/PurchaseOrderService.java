package com.fatema.procurement.service;

import com.fatema.procurement.dto.OrderItemDTO;
import com.fatema.procurement.dto.PurchaseOrderRequestDTO;
import com.fatema.procurement.entity.*;
import com.fatema.procurement.repository.*;
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

    // Создать заказ из страницы "Остаток/Закупка"

    @Transactional
    public PurchaseOrder createOrderFromStock(PurchaseOrderRequestDTO request) {
        // 1. Проверяем поставщика
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Поставщик не найден"));

        // 2. Создаём новый заказ
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setSupplier(supplier);
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.ZERO);  // ← ВАЖНО! Инициализируем сумму

        // 3. Сохраняем заказ
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        // 4. Добавляем товары и считаем сумму
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderRequestDTO.OrderItemRequestDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден: " + itemDTO.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getCostPrice());

            orderItemRepository.save(orderItem);

            // Считаем сумму
            BigDecimal itemTotal = product.getCostPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 5. Обновляем сумму заказа
        savedOrder.setTotalAmount(totalAmount);
        return purchaseOrderRepository.save(savedOrder);
    }

    // Создать заказ с товарами (старый метод)
    @Transactional
    public PurchaseOrder createPurchaseOrderWithItems(PurchaseOrder order, List<OrderItemDTO> items) {
        order.setOrderNumber(generateOrderNumber());

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.DRAFT);
        }

        if (order.getSupplier() != null && order.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(order.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : items) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getCostPrice());  // ← ИСПРАВЛЕНО

            orderItemRepository.save(orderItem);

            // ← ИСПРАВЛЕНО: используем costPrice
            totalAmount = totalAmount.add(product.getCostPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        savedOrder.setTotalAmount(totalAmount);
        return purchaseOrderRepository.save(savedOrder);
    }

    // Создать простой заказ (без товаров)
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        if (order.getSupplier() != null && order.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(order.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.DRAFT);
        }

        return purchaseOrderRepository.save(order);
    }

    // Обновить статус заказа (с пополнением склада при получении)
    @Transactional
    public PurchaseOrder updateOrderStatus(Long id, OrderStatus newStatus) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (newStatus == OrderStatus.DELIVERED && order.getStatus() != OrderStatus.DELIVERED) {
            addProductsToStock(order);
        }

        order.setStatus(newStatus);
        return purchaseOrderRepository.save(order);
    }

    // Добавить товары на склад
    private void addProductsToStock(PurchaseOrder order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getCurrentStock() + item.getQuantity();
            product.setCurrentStock(newStock);
            productRepository.save(product);
        }
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

    // Удалить заказ
    public void deletePurchaseOrder(Long id) {
        purchaseOrderRepository.deleteById(id);
    }
}
