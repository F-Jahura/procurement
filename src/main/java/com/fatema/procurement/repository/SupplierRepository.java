package com.fatema.procurement.repository;

import com.fatema.procurement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    // Поиск по названию (игнорируя регистр, частичное совпадение)
    List<Supplier> findByNameContainingIgnoreCase(String name);

    // Поиск по стране (точное совпадение)
    List<Supplier> findByCountryIgnoreCase(String country);

    // Поиск по статусу активности
    List<Supplier> findByActive(boolean active);

    // Поиск по названию и стране (комбинированный)
    List<Supplier> findByNameContainingIgnoreCaseAndCountryIgnoreCase(String name, String country);

    // Поиск по названию, стране и статусу
    List<Supplier> findByNameContainingIgnoreCaseAndCountryIgnoreCaseAndActive(String name, String country, boolean active);

    // Поиск всех поставщиков с сортировкой по дате создания (новые сначала)
    List<Supplier> findAllByOrderByCreatedAtDesc();

    // Поиск по стране с сортировкой по названию
    List<Supplier> findByCountryIgnoreCaseOrderByNameAsc(String country);

    // Комбинированный поиск с сортировкой
    List<Supplier> findByNameContainingIgnoreCaseAndCountryIgnoreCaseOrderByCreatedAtDesc(String name, String country);


    // Поиск поставщика по названию (игнорируя регистр)
    Optional<Supplier> findByNameIgnoreCase(String name);

    // Поиск всех активных поставщиков
    List<Supplier> findByActiveTrue();

    // Поиск поставщиков по стране
    List<Supplier> findByCountry(String country);

    // Проверка существования поставщика по email
    boolean existsByEmail(String email);
}
