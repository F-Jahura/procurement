package com.fatema.procurement.controller;

import com.fatema.procurement.dto.PurchaseOrderRequestDTO;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.service.ProductService;
import com.fatema.procurement.service.PurchaseOrderService;
import com.fatema.procurement.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stock-purchase")
public class StockPurchaseController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public String stockPurchasePage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer salesPeriod,
            @RequestParam(required = false) Boolean lowStockOnly,
            Model model) {

        // Получаем все товары
        List<Product> products = productService.getAllProducts();

        // ===== ФИЛЬТРАЦИЯ ПО ОСТАТКАМ =====
        if (lowStockOnly != null) {
            if (lowStockOnly) {
                // ТОЛЬКО КРИТИЧЕСКИЕ (остаток <= минимальный остаток)
                products = products.stream()
                        .filter(p -> p.getCurrentStock() != null && p.getMinStock() != null)
                        .filter(p -> p.getCurrentStock() <= p.getMinStock())
                        .collect(Collectors.toList());
            } else {
                // НОРМАЛЬНЫЙ ОСТАТОК (НЕ критические, т.е. остаток > минимальный остаток)
                products = products.stream()
                        .filter(p -> p.getCurrentStock() != null && p.getMinStock() != null)
                        .filter(p -> p.getCurrentStock() > p.getMinStock())
                        .collect(Collectors.toList());
            }
        }
        // Если lowStockOnly == null - показываем все товары (без фильтрации)

        // Фильтр по названию
        if (name != null && !name.isEmpty()) {
            String nameLower = name.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(nameLower))
                    .collect(Collectors.toList());
        }

        // Фильтр по поставщику
        if (supplierId != null) {
            products = products.stream()
                    .filter(p -> p.getSupplier() != null && p.getSupplier().getId().equals(supplierId))
                    .collect(Collectors.toList());
        }

        // Получаем всех поставщиков для выпадающего списка
        List<Supplier> suppliers = supplierService.getAllSuppliers();

        // Добавляем атрибуты в модель
        model.addAttribute("products", products);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("selectedSupplierId", supplierId);
        model.addAttribute("salesPeriod", salesPeriod != null ? salesPeriod : 3);
        model.addAttribute("name", name);
        model.addAttribute("lowStockOnly", lowStockOnly);

        return "stock-purchase/list";
    }

    @PostMapping("/create-order")
    public String createOrder(@RequestParam Long supplierId,
                              @RequestParam List<Long> selectedProducts,
                              @RequestParam Map<String, String> allParams,
                              RedirectAttributes redirectAttributes) {
        try {
            if (supplierId == null) {
                throw new RuntimeException("Пожалуйста, выберите поставщика");
            }

            List<PurchaseOrderRequestDTO.OrderItemRequestDTO> items = new ArrayList<>();
            for (Long productId : selectedProducts) {
                String quantityKey = "quantity_" + productId;
                String quantityStr = allParams.get(quantityKey);
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0) {
                        PurchaseOrderRequestDTO.OrderItemRequestDTO item = new PurchaseOrderRequestDTO.OrderItemRequestDTO();
                        item.setProductId(productId);
                        item.setQuantity(quantity);
                        items.add(item);
                    }
                }
            }

            if (items.isEmpty()) {
                throw new RuntimeException("Не выбрано ни одного товара для заказа");
            }

            PurchaseOrderRequestDTO request = new PurchaseOrderRequestDTO();
            request.setSupplierId(supplierId);
            request.setItems(items);

            purchaseOrderService.createOrderFromStock(request);
            redirectAttributes.addFlashAttribute("success", "Заказ успешно создан!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании заказа: " + e.getMessage());
        }
        return "redirect:/purchase-orders";
    }
}
