package com.fatema.procurement.repository;

import com.fatema.procurement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // ====== СУЩЕСТВУЮЩИЕ МЕТОДЫ ======
    List<Supplier> findByNameContainingIgnoreCase(String name);
    List<Supplier> findByCountryIgnoreCase(String country);
    List<Supplier> findByActive(Boolean active);
    List<Supplier> findByNameContainingIgnoreCaseAndCountryIgnoreCase(String name, String country);
    List<Supplier> findByNameContainingIgnoreCaseAndCountryIgnoreCaseAndActive(String name, String country, boolean active);
    List<Supplier> findAllByOrderByCreatedAtDesc();
    List<Supplier> findByCountryIgnoreCaseOrderByNameAsc(String country);
    List<Supplier> findByNameContainingIgnoreCaseAndCountryIgnoreCaseOrderByCreatedAtDesc(String name, String country);
    List<Supplier> findByActiveTrue();
    List<Supplier> findByCountry(String country);
    boolean existsByEmail(String email);

    // ====== НОВЫЕ МЕТОДЫ ДЛЯ ИМПОРТА ======

    // Поиск по названию (игнорируя регистр) - возвращает список
    List<Supplier> findByNameIgnoreCase(String name);

    // Поиск по названию (игнорируя регистр) и стране (игнорируя регистр)
    Optional<Supplier> findByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

    // Поиск по email (игнорируя регистр)
    Optional<Supplier> findByEmailIgnoreCase(String email);

    // Поиск по телефону
    Optional<Supplier> findByPhone(String phone);

    // Проверка существования по email, исключая указанный ID
    boolean existsByEmailAndIdNot(String email, Long id);

    // Поиск по названию с сортировкой по дате создания (новые сначала)
    List<Supplier> findByNameIgnoreCaseOrderByCreatedAtDesc(String name);
}
