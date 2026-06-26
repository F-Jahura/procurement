package com.fatema.procurement.controller;

import com.fatema.procurement.dto.ProductFilterDTO;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.service.ProductService;
import com.fatema.procurement.service.SalesCalculationService;
import com.fatema.procurement.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class ProductControllerUI {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SalesCalculationService salesCalculationService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(required = false) Integer salesPeriodMonths,
            Model model) {

        // Обработка параметров
        if (name != null && name.trim().isEmpty()) name = null;
        if (minStock != null && minStock <= 0) minStock = null;

        int period = salesPeriodMonths != null ? salesPeriodMonths : 3;
        if (period < 1) period = 1;
        if (period > 6) period = 6;

        ProductFilterDTO filter = new ProductFilterDTO(name, supplierId, minStock, lowStockOnly, period);

        // Получаем товары
        List<Product> products = productService.getFilteredProducts(filter);

        // Считаем продажи для каждого товара
        Map<Long, Integer> salesMap = new HashMap<>();
        for (Product p : products) {
            try {
                int sales = salesCalculationService.getSalesCountForProduct(p.getId(), period);
                salesMap.put(p.getId(), sales);
            } catch (Exception e) {
                salesMap.put(p.getId(), 0);
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("filter", filter);
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("salesMap", salesMap);
        model.addAttribute("salesPeriod", period);

        return "products/list";
    }

    /*@GetMapping
    public String listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(required = false) Integer salesPeriodMonths,
            Model model) {

        // ВРЕМЕННО: просто показываем все товары без продаж
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("salesMap", new HashMap<>());

        return "products/list";
    }*/
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        return "products/form";
    }

    @PostMapping
    public String createProduct(@ModelAttribute Product product) {
        if (product.getSupplierId() != null) {
            product.setSupplier(supplierService.getSupplierById(product.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found")));
        }
        productService.createProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        return "products/form";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product) {
        if (product.getSupplierId() != null) {
            product.setSupplier(supplierService.getSupplierById(product.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found")));
        }
        productService.updateProduct(id, product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}
