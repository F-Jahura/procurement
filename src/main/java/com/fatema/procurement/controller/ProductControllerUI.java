package com.fatema.procurement.controller;

import com.fatema.procurement.dto.ImportResultDTO;
import com.fatema.procurement.dto.ProductFilterDTO;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.repository.ProductRepository;
import com.fatema.procurement.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class  ProductControllerUI {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private SalesCalculationService salesCalculationService;

    @Autowired
    private ExcelImportService excelImportService;
    @Autowired
    private ExcelExportService excelExportService;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/export/excel")
    public void exportProducts(HttpServletResponse response) throws IOException {
        List<Product> products = productRepository.findAll();
        excelExportService.exportProducts(products, response);
    }
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,  // ← НОВЫЙ ПАРАМЕТР
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Boolean lowStockOnly,
            @RequestParam(required = false) Integer salesPeriodMonths,
            Model model) {

        if (name != null && name.trim().isEmpty()) name = null;
        if (sku != null && sku.trim().isEmpty()) sku = null;  // ← ДОБАВЛЕНО
        if (minStock != null && minStock <= 0) minStock = null;

        int period = salesPeriodMonths != null ? salesPeriodMonths : 3;
        if (period < 1) period = 1;
        if (period > 6) period = 6;

        ProductFilterDTO filter = new ProductFilterDTO(name, supplierId, minStock, lowStockOnly);
        filter.setSku(sku);  // ← ДОБАВЛЕНО
        filter.setSalesPeriodMonths(period);

        List<Product> products = productService.getFilteredProducts(filter);

        if (lowStockOnly != null && lowStockOnly) {
            products = products.stream()
                    .filter(p -> "Критично!".equals(productService.getStockStatusText(p)))
                    .collect(Collectors.toList());
        }

        Map<Long, String> statusTextMap = new HashMap<>();
        Map<Long, String> statusColorMap = new HashMap<>();
        for (Product p : products) {
            statusTextMap.put(p.getId(), productService.getStockStatusText(p));
            statusColorMap.put(p.getId(), productService.getStockStatusColor(p));
        }

        model.addAttribute("products", products);
        model.addAttribute("filter", filter);
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("statusTextMap", statusTextMap);
        model.addAttribute("statusColorMap", statusColorMap);
        model.addAttribute("salesPeriod", period);

        return "products/list";
    }

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

    @PostMapping("/import")
    public String importProducts(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        ImportResultDTO result = excelImportService.importProducts(file);
        redirectAttributes.addFlashAttribute("importResult", result);
        return "redirect:/products";
    }
}
