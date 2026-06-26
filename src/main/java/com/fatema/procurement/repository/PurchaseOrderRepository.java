package com.fatema.procurement.repository;

import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Поиск заказов по номеру
    List<PurchaseOrder> findByOrderNumberContaining(String orderNumber);

    // Поиск заказов по статусу
    List<PurchaseOrder> findByStatus(OrderStatus status);

    // Поиск заказов по дате (диапазон)
    List<PurchaseOrder> findByOrderDateBetween(LocalDate start, LocalDate end);

    // Поиск заказов по поставщику
    List<PurchaseOrder> findBySupplierId(Long supplierId);

    // Поиск заказов, которые должны быть доставлены в ближайшие 7 дней
    List<PurchaseOrder> findByDeliveryDateBetween(LocalDate start, LocalDate end);

    List<PurchaseOrder> findByStatusAndDeliveryDateBetween(OrderStatus status, LocalDate startDate, LocalDate endDate);
}