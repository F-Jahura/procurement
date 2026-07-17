package com.fatema.procurement.service;

import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.entity.PurchaseOrder;
import com.fatema.procurement.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Получить все активные заказы (не завершённые и не отменённые)
     */
    public List<PurchaseOrder> getActiveOrders() {
        return purchaseOrderRepository.findAll().stream()
                .filter(order -> order.getStatus() != OrderStatus.DELIVERED
                        && order.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    /**
     * Получить все завершённые заказы (доставленные)
     */
    public List<PurchaseOrder> getCompletedOrders() {
        return purchaseOrderRepository.findByStatus(OrderStatus.DELIVERED);
    }

    /**
     * Получить все отменённые заказы
     */
    public List<PurchaseOrder> getCancelledOrders() {
        return purchaseOrderRepository.findByStatus(OrderStatus.CANCELLED);
    }

    /**
     * Получить заказы по статусу
     */
    public List<PurchaseOrder> getOrdersByStatus(OrderStatus status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    /**
     * Получить общую сумму всех заказов
     */
    public BigDecimal getTotalOrdersAmount() {
        return purchaseOrderRepository.findAll().stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Получить количество активных заказов
     */
    public long getActiveOrdersCount() {
        return getActiveOrders().size();
    }

    /**
     * Получить общую сумму активных заказов
     */
    public BigDecimal getActiveOrdersTotalAmount() {
        return getActiveOrders().stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
