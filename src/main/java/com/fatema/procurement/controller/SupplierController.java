package com.fatema.procurement.controller;

import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // GET /api/suppliers — получить всех поставщиков
    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    // GET /api/suppliers/active — получить активных поставщиков
    @GetMapping("/active")
    public List<Supplier> getActiveSuppliers() {
        return supplierService.getActiveSuppliers();
    }

    // GET /api/suppliers/{id} — получить поставщика по ID
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return supplierService.getSupplierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/suppliers/search?name=... — поиск по названию
    @GetMapping("/search")
    public ResponseEntity<Supplier> getSupplierByName(@RequestParam String name) {
        return supplierService.getSupplierByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/suppliers/country/{country} — поставщики по стране
    @GetMapping("/country/{country}")
    public List<Supplier> getSuppliersByCountry(@PathVariable String country) {
        return supplierService.getSuppliersByCountry(country);
    }

    // POST /api/suppliers — создать поставщика
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        System.out.println("Received: " + supplier);
        // Проверяем, что email не занят
        if (supplier.getEmail() != null && supplierService.existsByEmail(supplier.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        Supplier created = supplierService.createSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/suppliers/{id} — обновить поставщика
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        try {
            Supplier updated = supplierService.updateSupplier(id, supplier);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /api/suppliers/{id}/deactivate — деактивировать поставщика (мягкое удаление)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateSupplier(@PathVariable Long id) {
        try {
            supplierService.deactivateSupplier(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/suppliers/{id} — удалить поставщика (жёсткое удаление)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
