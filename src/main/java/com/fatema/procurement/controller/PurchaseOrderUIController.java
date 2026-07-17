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

@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderUIController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", purchaseOrderService.getAllOrders());
        model.addAttribute("statuses", OrderStatus.values());
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
