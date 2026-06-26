package com.fatema.procurement.repository;

import com.fatema.procurement.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Найти все товары в заказе
    List<OrderItem> findByOrderId(Long orderId);

    // Найти все заказы, в которых есть товар
    List<OrderItem> findByProductId(Long productId);

    // Подсчитать общее количество товара в доставленных заказах за период
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "WHERE oi.product.id = :productId " +
            "AND oi.order.status = 'DELIVERED' " +
            "AND oi.order.deliveryDate BETWEEN :startDate AND :endDate")
    int sumQuantityForProductInDeliveredOrders(
            @Param("productId") Long productId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
