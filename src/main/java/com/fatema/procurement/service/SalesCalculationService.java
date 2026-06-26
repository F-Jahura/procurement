package com.fatema.procurement.service;

import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.entity.PurchaseOrder;
import com.fatema.procurement.repository.OrderItemRepository;
import com.fatema.procurement.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalesCalculationService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * Подсчитывает количество продаж для каждого товара за указанный период (месяцы)
     * @param months количество месяцев (1-6)
     * @return Map<productId, salesCount>
     */
    public Map<Long, Integer> calculateSalesByPeriod(int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        // Получаем все доставленные заказы за период
        List<PurchaseOrder> deliveredOrders = purchaseOrderRepository
                .findByStatusAndDeliveryDateBetween(OrderStatus.DELIVERED, startDate, LocalDate.now());

        // Здесь нужно получить товары из заказов и подсчитать количество
        // Для упрощения используем заглушку:
        // В реальном проекте нужно хранить связь заказ-товар (OrderItem)
        // Сейчас вернём пустой Map, но покажем как должно работать

        // Пример: загружаем все заказы и считаем
        /*Map<Long, Integer> salesMap = deliveredOrders.stream()
                .flatMap(order -> {
                    // Здесь должен быть список товаров в заказе
                    // Пока используем заглушку
                    return java.util.stream.Stream.empty();
                })
                .collect(Collectors.toMap(
                        product -> product.getId(),
                        product -> 1,
                        Integer::sum
                ));

        return salesMap;*/

        return new HashMap<>();
    }

    /**
     * Получает количество продаж для конкретного товара
     */
    public int getSalesCountForProduct(Long productId, int months) {
        try {
            LocalDate startDate = LocalDate.now().minusMonths(months);
            LocalDate endDate = LocalDate.now();
            return orderItemRepository.sumQuantityForProductInDeliveredOrders(productId, startDate, endDate);
        } catch (Exception e) {
            return 0;
        }
    }
}

