package com.fatema.procurement.service;

import com.fatema.procurement.dto.SupplierFilterDTO;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    // Поиск с фильтрацией
    public List<Supplier> getFilteredSuppliers(SupplierFilterDTO filter) {
        String name = filter.getName() != null ? filter.getName().trim() : null;
        String country = filter.getCountry() != null ? filter.getCountry().trim() : null;
        Boolean active = filter.getActive();

        // Если все поля пустые → возвращаем все с сортировкой
        if ((name == null || name.isEmpty()) && (country == null || country.isEmpty()) && active == null) {
            return supplierRepository.findAllByOrderByCreatedAtDesc();
        }

        // Если есть и название, и страна, и статус
        if (name != null && !name.isEmpty() && country != null && !country.isEmpty() && active != null) {
            return supplierRepository.findByNameContainingIgnoreCaseAndCountryIgnoreCaseAndActive(
                    name, country, active
            );
        }

        // Если есть название и страна (без статуса)
        if (name != null && !name.isEmpty() && country != null && !country.isEmpty()) {
            return supplierRepository.findByNameContainingIgnoreCaseAndCountryIgnoreCaseOrderByCreatedAtDesc(
                    name, country
            );
        }

        // Если есть только название
        if (name != null && !name.isEmpty()) {
            return supplierRepository.findByNameContainingIgnoreCase(name);
        }

        // Если есть только страна
        if (country != null && !country.isEmpty()) {
            return supplierRepository.findByCountryIgnoreCase(country);
        }

        // Если есть только статус
        if (active != null) {
            return supplierRepository.findByActive(active);
        }

        return supplierRepository.findAllByOrderByCreatedAtDesc();
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
        return supplierRepository.findByNameIgnoreCase(name);
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

