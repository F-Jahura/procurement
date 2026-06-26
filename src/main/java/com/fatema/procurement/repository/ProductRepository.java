package com.fatema.procurement.repository;

import com.fatema.procurement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findBySupplierId(Long supplierId);
    List<Product> findByPriceLessThan(BigDecimal price);
    List<Product> findByCurrentStockLessThanEqual(Integer minStock);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByNameContainingIgnoreCaseAndSupplierId(String name, Long supplierId);

    // Товары с остатком меньше указанного значения
    List<Product> findByCurrentStockLessThan(Integer stock);


    // Все товары с сортировкой по остатку (сначала самые маленькие)
    List<Product> findAllByOrderByCurrentStockAsc();

    // Товары с остатком < 10 (критично)
    @Query("SELECT p FROM Product p WHERE p.currentStock < 10")
    List<Product> findCriticalStock();

    // Товары с остатком < 30 (мало)
    @Query("SELECT p FROM Product p WHERE p.currentStock BETWEEN 10 AND 29")
    List<Product> findLowStock();

    // Поиск с фильтрацией (используем для комбинированных запросов)
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:supplierId IS NULL OR p.supplier.id = :supplierId) AND " +
            "(:minStock IS NULL OR p.currentStock < :minStock)")
    List<Product> findWithFilters(@Param("name") String name,
                                  @Param("supplierId") Long supplierId,
                                  @Param("minStock") Integer minStock);

    // Товары с остатком < 10 (для фильтра lowStockOnly)
    @Query("SELECT p FROM Product p WHERE p.currentStock < 10")
    List<Product> findLowStockOnly();
}
