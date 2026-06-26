package com.fatema.procurement.controller;

import com.fatema.procurement.entity.Product;
import com.fatema.procurement.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/products — получить все товары
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // GET /api/products/{id} — получить товар по ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/products/sku/{sku} — поиск по артикулу
    @GetMapping("/sku/{sku}")
    public Optional<Product> getProductsBySku(@PathVariable String sku) {
        return productService.getProductsBySku(sku);
    }

    // GET /api/products/supplier/{supplierId} — товары по поставщику
    @GetMapping("/supplier/{supplierId}")
    public List<Product> getProductsBySupplier(@PathVariable Long supplierId) {
        return productService.getProductsBySupplier(supplierId);
    }

    // GET /api/products/price-less-than?price=100 — товары дешевле цены
    @GetMapping("/price-less-than")
    public List<Product> getProductsCheaperThan(@RequestParam BigDecimal price) {
        return productService.getProductsCheaperThan(price);
    }

    // GET /api/products/low-stock — товары с низким остатком
    @GetMapping("/low-stock")
    public List<Product> getProductsWithLowStock() {
        return productService.getProductsWithLowStock();
    }

    // GET /api/products/below-min-stock — товары ниже минимального остатка
    @GetMapping("/below-min-stock")
    public List<Product> getProductsBelowMinStock() {
        return productService.getProductsBelowMinStock();
    }

    // POST /api/products — создать товар
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/products/{id} — обновить товар
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /api/products/{id}/stock — обновить остаток
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestParam int stock) {
        try {
            Product updated = productService.updateStock(id, stock);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/products/{id} — удалить товар
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
