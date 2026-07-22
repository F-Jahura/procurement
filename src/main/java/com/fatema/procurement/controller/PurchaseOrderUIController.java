package com.fatema.procurement.controller;

import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.entity.PurchaseOrder;
import com.fatema.procurement.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderUIController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String status,
            Model model) {

        List<PurchaseOrder> orders;

        // Фильтрация по статусу
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = purchaseOrderService.getOrdersByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                // Если статус не найден - показываем все заказы
                orders = purchaseOrderService.getAllOrders();
            }
        } else {
            orders = purchaseOrderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "purchase-orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        PurchaseOrder order = purchaseOrderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        model.addAttribute("order", order);
        return "purchase-orders/details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            PurchaseOrder order = purchaseOrderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Статус заказа #" + order.getOrderNumber() + " обновлён на: " + status.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchase-orders";
    }
}
