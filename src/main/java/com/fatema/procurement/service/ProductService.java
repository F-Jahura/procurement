package com.fatema.procurement.service;

import com.fatema.procurement.dto.ProductFilterDTO;
import com.fatema.procurement.dto.ProductWithSalesDTO;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.ProductRepository;
import com.fatema.procurement.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SalesCalculationService salesCalculationService;

    @Autowired
    private SupplierRepository supplierRepository;


    public List<ProductWithSalesDTO> getProductsWithSales(ProductFilterDTO filter) {
        List<Product> products = getFilteredProducts(filter);
        final int salesPeriod = filter.getSalesPeriodMonths() != null ? filter.getSalesPeriodMonths() : 3;

        return products.stream()
                .map(product -> {
                    int sales = salesCalculationService.getSalesCountForProduct(product.getId(), salesPeriod);
                    return new ProductWithSalesDTO(product, sales);
                })
                .collect(Collectors.toList());
    }

    public List<Product> getFilteredProducts(ProductFilterDTO filter) {
        // Если фильтр пустой — возвращаем все
        if (filter == null || filter.isEmpty()) {
            return productRepository.findAllByOrderByCurrentStockAsc();
        }

        String name = filter.getName() != null ? filter.getName().trim() : null;
        Long supplierId = filter.getSupplierId();
        Integer minStock = filter.getMinStock();

        // Если включен фильтр "только низкий остаток"
        if (filter.getLowStockOnly() != null && filter.getLowStockOnly()) {
            return productRepository.findLowStockOnly();
        }

        // Начинаем с полного списка
        List<Product> result = productRepository.findAll();

        // Фильтр по названию (если есть)
        if (name != null && !name.isEmpty()) {
            String nameLower = name.toLowerCase();
            result = result.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(nameLower))
                    .collect(Collectors.toList());
        }

        // Фильтр по поставщику (если есть)
        if (supplierId != null) {
            result = result.stream()
                    .filter(p -> p.getSupplier() != null && p.getSupplier().getId().equals(supplierId))
                    .collect(Collectors.toList());
        }

        // Фильтр по минимальному остатку (если есть)
        if (minStock != null && minStock > 0) {
            result = result.stream()
                    .filter(p -> p.getCurrentStock() != null && p.getCurrentStock() < minStock)
                    .collect(Collectors.toList());
        }

        // Сортировка по остатку (сначала самые маленькие)
        result.sort(Comparator.comparingInt(p -> p.getCurrentStock() != null ? p.getCurrentStock() : Integer.MAX_VALUE));

        return result;
    }

    // Получить все товары с низким остатком (10-29)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStock();
    }

    // Получить цвет статуса для товара
    public String getStockStatusColor(Product product) {
        Integer stock = product.getCurrentStock();
        if (stock == null) return "bg-secondary";
        if (stock < 10) return "bg-danger";
        if (stock < 30) return "bg-warning text-dark";
        return "bg-success";
    }

    // Получить текст статуса для товара
    public String getStockStatusText(Product product) {
        Integer stock = product.getCurrentStock();
        if (stock == null) return "Неизвестно";
        if (stock < 10) return "Критично!";
        if (stock < 30) return "Мало";
        return "Норма";
    }

    // ... остальные методы (create, update, delete, etc.)
    public Product createProduct(Product product) {
        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            product.setSupplier(supplier);
        }
        if (product.getCurrentStock() == null) {
            product.setCurrentStock(0);
        }
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductsBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public List<Product> getProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    public List<Product> getProductsCheaperThan(BigDecimal price) {
        return productRepository.findByPriceLessThan(price);
    }

    public List<Product> getProductsWithLowStock() {
        return productRepository.findByCurrentStockLessThanEqual(0);
    }

    public List<Product> getProductsBelowMinStock() {
        return productRepository.findAll().stream()
                .filter(p -> p.getMinStock() != null && p.getCurrentStock() != null &&
                        p.getCurrentStock() < p.getMinStock())
                .toList();
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setSku(productDetails.getSku());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setMinStock(productDetails.getMinStock());
        product.setCurrentStock(productDetails.getCurrentStock());
        product.setUnit(productDetails.getUnit());

        if (productDetails.getSupplier() != null && productDetails.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(productDetails.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            product.setSupplier(supplier);
        }

        return productRepository.save(product);
    }
    public Product updateStock(Long id, int newStock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setCurrentStock(newStock);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

