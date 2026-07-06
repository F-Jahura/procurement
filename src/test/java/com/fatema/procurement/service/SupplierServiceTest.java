package com.fatema.procurement.service;

import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Тестовый поставщик");
        supplier.setCountry("Россия");
        supplier.setEmail("test@example.com");
        supplier.setActive(true);
    }

    @Test
    void testFindAllSuppliers() {
        when(supplierRepository.findAll()).thenReturn(Arrays.asList(supplier));

        List<Supplier> suppliers = supplierService.findAll();

        assertNotNull(suppliers);
        assertEquals(1, suppliers.size());
        assertEquals("Тестовый поставщик", suppliers.get(0).getName());
    }

    @Test
    void testFindById() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        Optional<Supplier> found = supplierService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Тестовый поставщик", found.get().getName());
    }

    @Test
    void testSaveSupplier() {
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        Supplier saved = supplierService.save(supplier);

        assertNotNull(saved);
        assertEquals("Тестовый поставщик", saved.getName());
    }

    @Test
    void testDeleteSupplier() {
        doNothing().when(supplierRepository).deleteById(1L);

        supplierService.delete(1L);

        verify(supplierRepository, times(1)).deleteById(1L);
    }
}
