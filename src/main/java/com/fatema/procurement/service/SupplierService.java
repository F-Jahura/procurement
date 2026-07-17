package com.fatema.procurement.service;

import com.fatema.procurement.dto.SupplierFilterDTO;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getFilteredSuppliers(SupplierFilterDTO filter) {
        // Получаем все записи
        List<Supplier> result = supplierRepository.findAll();

        System.out.println("=== ФИЛЬТРАЦИЯ ===");
        System.out.println("Всего записей: " + result.size());
        System.out.println("filter.getActive(): " + filter.getActive());

        // Фильтр по имени
        if (filter.getName() != null && !filter.getName().isEmpty()) {
            String nameLower = filter.getName().toLowerCase().trim();
            result = result.stream()
                    .filter(s -> s.getName() != null &&
                            s.getName().toLowerCase().contains(nameLower))
                    .collect(Collectors.toList());
            System.out.println("После фильтра по имени: " + result.size());
        }

        // Фильтр по стране
        if (filter.getCountry() != null && !filter.getCountry().isEmpty()) {
            String countryLower = filter.getCountry().toLowerCase().trim();
            result = result.stream()
                    .filter(s -> s.getCountry() != null &&
                            s.getCountry().toLowerCase().equals(countryLower))
                    .collect(Collectors.toList());
            System.out.println("После фильтра по стране: " + result.size());
        }

        // Фильтр по статусу (ИСПРАВЛЕНО!)
        if (filter.getActive() != null) {
            System.out.println("Применяем фильтр по статусу: " + filter.getActive());
            result = result.stream()
                    .filter(s -> {
                        // Явно получаем значение active
                        boolean isActive = s.isActive();  // или s.getActive()
                        boolean match = isActive == filter.getActive();
                        System.out.println("  - " + s.getName() + " | active: " + isActive + " | match: " + match);
                        return match;
                    })
                    .collect(Collectors.toList());
            System.out.println("После фильтра по статусу: " + result.size());
        }

        // Сортировка по дате создания (новые сверху)
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        System.out.println("Итоговый результат: " + result.size());
        return result;
    }

    // Получить все уникальные страны для фильтра
    public List<String> getAllCountries() {
        return supplierRepository.findAll().stream()
                .map(Supplier::getCountry)
                .filter(country -> country != null && !country.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    // Создать поставщика
    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    // Получить всех поставщиков
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // Получить только активных поставщиков
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByActiveTrue();
    }

    // Получить поставщика по ID
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    // Получить поставщика по названию
    public Optional<Supplier> getSupplierByName(String name) {
        List<Supplier> suppliers = supplierRepository.findByNameIgnoreCase(name);
        return suppliers.isEmpty() ? Optional.empty() : Optional.of(suppliers.get(0));
    }

    // Получить поставщиков по стране
    public List<Supplier> getSuppliersByCountry(String country) {
        return supplierRepository.findByCountry(country);
    }

    // Обновить поставщика
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

        supplier.setName(supplierDetails.getName());
        supplier.setCountry(supplierDetails.getCountry());
        supplier.setContactPerson(supplierDetails.getContactPerson());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setPhone(supplierDetails.getPhone());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setTaxId(supplierDetails.getTaxId());
        supplier.setActive(supplierDetails.isActive());

        return supplierRepository.save(supplier);
    }

    // Удалить поставщика (мягкое удаление — делаем неактивным)
    public void deactivateSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    // Полностью удалить поставщика (жёсткое удаление)
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }

    // Проверить существование по email
    public boolean existsByEmail(String email) {
        return supplierRepository.existsByEmail(email);
    }
}

