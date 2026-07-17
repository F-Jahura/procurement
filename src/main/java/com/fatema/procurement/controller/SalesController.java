package com.fatema.procurement.controller;

import com.fatema.procurement.service.ProductService;
import com.fatema.procurement.service.SellOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellOrderService sellOrderService;

    @GetMapping("/products")
    public String salesProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "sales/products";
    }

    @GetMapping("/orders")
    public String salesOrders(Model model) {
        model.addAttribute("orders", sellOrderService.getActiveOrders());
        return "sales/orders";
    }

    @GetMapping("/reports")
    public String salesReports(Model model) {
        // Статистика для отчётов
        model.addAttribute("activeOrdersCount", sellOrderService.getActiveOrdersCount());
        model.addAttribute("totalOrdersAmount", sellOrderService.getTotalOrdersAmount());
        model.addAttribute("activeOrdersTotalAmount", sellOrderService.getActiveOrdersTotalAmount());
        model.addAttribute("completedOrders", sellOrderService.getCompletedOrders());
        model.addAttribute("cancelledOrders", sellOrderService.getCancelledOrders());
        return "sales/reports";
    }
}


