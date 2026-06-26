package com.fatema.procurement.service;

import com.fatema.procurement.dto.DashboardStats;
import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.repository.ProductRepository;
import com.fatema.procurement.repository.PurchaseOrderRepository;
import com.fatema.procurement.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    public DashboardStats getStats() {
        long suppliers = supplierRepository.count();
        long products = productRepository.count();
        long activeOrders = purchaseOrderRepository.findByStatus(OrderStatus.CONFIRMED).size()
                + purchaseOrderRepository.findByStatus(OrderStatus.SENT).size()
                + purchaseOrderRepository.findByStatus(OrderStatus.SHIPPED).size();

        // Сумма всех заказов (можно добавить метод в репозиторий позже)
        BigDecimal totalAmount = purchaseOrderRepository.findAll().stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStats(suppliers, products, activeOrders, totalAmount);
    }
}
