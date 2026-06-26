package com.fatema.procurement.controller;

import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.entity.PurchaseOrder;
import com.fatema.procurement.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // GET /api/purchase-orders — получить все заказы
    @GetMapping
    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderService.getAllOrders();
    }

    // GET /api/purchase-orders/{id} — получить заказ по ID
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getOrderById(@PathVariable Long id) {
        return purchaseOrderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/purchase-orders/search?number=... — поиск по номеру заказа
    @GetMapping("/search")
    public List<PurchaseOrder> getOrdersByNumber(@RequestParam String number) {
        return purchaseOrderService.getOrdersByNumber(number);
    }

    // GET /api/purchase-orders/status/{status} — заказы по статусу
    @GetMapping("/status/{status}")
    public List<PurchaseOrder> getOrdersByStatus(@PathVariable OrderStatus status) {
        return purchaseOrderService.getOrdersByStatus(status);
    }

    // GET /api/purchase-orders/supplier/{supplierId} — заказы по поставщику
    @GetMapping("/supplier/{supplierId}")
    public List<PurchaseOrder> getOrdersBySupplier(@PathVariable Long supplierId) {
        return purchaseOrderService.getOrdersBySupplier(supplierId);
    }

    // GET /api/purchase-orders/date-range?start=...&end=... — заказы по диапазону дат
    @GetMapping("/date-range")
    public List<PurchaseOrder> getOrdersByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return purchaseOrderService.getOrdersByDateRange(startDate, endDate);
    }

    // GET /api/purchase-orders/delivering-soon — заказы с доставкой в ближайшие 7 дней
    @GetMapping("/delivering-soon")
    public List<PurchaseOrder> getOrdersDeliveringSoon() {
        return purchaseOrderService.getOrdersDeliveringSoon();
    }

    // POST /api/purchase-orders — создать заказ
    @PostMapping
    public ResponseEntity<PurchaseOrder> createOrder(@RequestBody PurchaseOrder order) {
        PurchaseOrder created = purchaseOrderService.createPurchaseOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/purchase-orders/{id} — обновить заказ
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrder> updateOrder(@PathVariable Long id, @RequestBody PurchaseOrder order) {
        try {
            PurchaseOrder updated = purchaseOrderService.updatePurchaseOrder(id, order);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /api/purchase-orders/{id}/status — обновить статус заказа
    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrder> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        try {
            PurchaseOrder updated = purchaseOrderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/purchase-orders/{id} — удалить заказ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deletePurchaseOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

