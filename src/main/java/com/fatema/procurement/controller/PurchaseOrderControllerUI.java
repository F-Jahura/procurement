package com.fatema.procurement.controller;

import com.fatema.procurement.dto.OrderItemDTO;
import com.fatema.procurement.entity.OrderStatus;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.entity.PurchaseOrder;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.service.ProductService;
import com.fatema.procurement.service.PurchaseOrderService;
import com.fatema.procurement.service.SalesCalculationService;
import com.fatema.procurement.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Controller
@RequestMapping("/orders")
public class PurchaseOrderControllerUI {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SalesCalculationService salesCalculationService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", purchaseOrderService.getAllOrders());
        return "orders/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new PurchaseOrder());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("products", productService.getAllProducts());
        return "orders/form";
    }

    @PostMapping
    public String createOrder(@ModelAttribute PurchaseOrder order,
                              @RequestParam(required = false) List<Long> productIds,
                              @RequestParam(required = false) List<Integer> quantities,
                              @RequestParam(required = false) Long supplierId) {

        if (supplierId != null) {
            Supplier supplier = supplierService.getSupplierById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        List<OrderItemDTO> items = new ArrayList<>();
        if (productIds != null && quantities != null) {
            for (int i = 0; i < productIds.size(); i++) {
                if (productIds.get(i) != null && quantities.get(i) != null && quantities.get(i) > 0) {
                    Product product = productService.getProductById(productIds.get(i))
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    OrderItemDTO item = new OrderItemDTO(
                            product.getId(),
                            product.getName(),
                            quantities.get(i),
                            product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO
                    );
                    items.add(item);
                }
            }
        }

        if (items.isEmpty()) {
            throw new RuntimeException("Заказ должен содержать хотя бы один товар");
        }

        purchaseOrderService.createPurchaseOrderWithItems(order, items);
        return "redirect:/orders";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PurchaseOrder order = purchaseOrderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("products", productService.getAllProducts());

        return "orders/form";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable Long id,
                              @ModelAttribute PurchaseOrder order,
                              @RequestParam(required = false) Long supplierId) {

        if (supplierId != null) {
            Supplier supplier = supplierService.getSupplierById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            order.setSupplier(supplier);
        }

        purchaseOrderService.updatePurchaseOrder(id, order);
        return "redirect:/orders";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return "redirect:/orders";
    }
}