package com.fatema.procurement.service;

import com.fatema.procurement.dto.ProductFilterDTO;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.ProductRepository;
import com.fatema.procurement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SalesCalculationService salesCalculationService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Тестовый поставщик");

        product = new Product();
        product.setId(1L);
        product.setName("Тестовый товар");
        product.setPrice(new BigDecimal("100.00"));
        product.setCurrentStock(50);
        product.setSupplier(supplier);
    }

    @Test
    void testFindAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        List<Product> products = productService.findAll();

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Тестовый товар", products.get(0).getName());
    }

    @Test
    void testFindById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> found = productService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Тестовый товар", found.get().getName());
    }

    @Test
    void testSaveProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = productService.save(product);

        assertNotNull(saved);
        assertEquals("Тестовый товар", saved.getName());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetLowStockProducts() {
        Product lowStockProduct = new Product();
        lowStockProduct.setId(2L);
        lowStockProduct.setName("Товар с низким остатком");
        lowStockProduct.setCurrentStock(5);
        lowStockProduct.setMinStock(10);

        when(productRepository.findAll()).thenReturn(Arrays.asList(lowStockProduct));

        List<Product> lowStock = productService.getLowStockProducts();

        assertNotNull(lowStock);
        assertEquals(1, lowStock.size());
        assertEquals("Товар с низким остатком", lowStock.get(0).getName());
    }
}
