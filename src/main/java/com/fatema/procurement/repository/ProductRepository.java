package com.fatema.procurement.repository;

import com.fatema.procurement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ====== СУЩЕСТВУЮЩИЕ МЕТОДЫ ======
    Optional<Product> findBySku(String sku);
    List<Product> findBySupplierId(Long supplierId);
    List<Product> findByCostPriceLessThan(BigDecimal price);
    List<Product> findByCurrentStockLessThanEqual(Integer minStock);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByNameContainingIgnoreCaseAndSupplierId(String name, Long supplierId);
    List<Product> findByCurrentStockLessThan(Integer stock);
    List<Product> findAllByOrderByCurrentStockAsc();

    @Query("SELECT p FROM Product p WHERE p.currentStock < 10")
    List<Product> findCriticalStock();

    @Query("SELECT p FROM Product p WHERE p.currentStock BETWEEN 10 AND 29")
    List<Product> findLowStock();

    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:supplierId IS NULL OR p.supplier.id = :supplierId) AND " +
            "(:minStock IS NULL OR p.currentStock < :minStock)")
    List<Product> findWithFilters(@Param("name") String name,
                                  @Param("supplierId") Long supplierId,
                                  @Param("minStock") Integer minStock);

    @Query("SELECT p FROM Product p WHERE p.currentStock < 10")
    List<Product> findLowStockOnly();

    @Modifying
    @Query("UPDATE Product p SET p.currentStock = p.currentStock + :quantity WHERE p.id = :productId")
    void addStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // ====== МЕТОДЫ ДЛЯ ИМПОРТА ======

    // Поиск по названию (игнорируя регистр) и поставщику — ИСПРАВЛЕНО!
    @Query("SELECT p FROM Product p WHERE UPPER(p.name) = UPPER(:name) AND p.supplier.id = :supplierId")
    Optional<Product> findByNameIgnoreCaseAndSupplierId(@Param("name") String name, @Param("supplierId") Long supplierId);

    // Проверка существования по названию (игнорируя регистр) и поставщику
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE UPPER(p.name) = UPPER(:name) AND p.supplier.id = :supplierId")
    boolean existsByNameIgnoreCaseAndSupplierId(@Param("name") String name, @Param("supplierId") Long supplierId);

    // Проверка существования по SKU
    boolean existsBySku(String sku);

    // Проверка существования по SKU, исключая указанный ID
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku AND p.id != :id")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("id") Long id);
}
