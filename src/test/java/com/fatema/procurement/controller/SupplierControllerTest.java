package com.fatema.procurement.controller;

import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Test
    @WithMockUser(username = "admin", roles = {"USER"})
    void testSupplierListPage() throws Exception {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Тестовый поставщик");

        when(supplierService.findAll()).thenReturn(Arrays.asList(supplier));

        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isOk())
                .andExpect(view().name("suppliers/list"))
                .andExpect(model().attributeExists("suppliers"));
    }

    @Test
    void testSupplierListPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/suppliers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
