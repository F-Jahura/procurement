package com.fatema.procurement.controller;

import com.fatema.procurement.dto.SupplierFilterDTO;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierControllerUI {

    @Autowired
    private SupplierService supplierService;

    // Страница со списком поставщиков с фильтрацией
    @GetMapping
    public String listSuppliers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean active,
            Model model) {

        // Создаём объект фильтра
        SupplierFilterDTO filter = new SupplierFilterDTO(name, country, active);

        // Получаем отфильтрованный список
        List<Supplier> suppliers = supplierService.getFilteredSuppliers(filter);

        // Получаем список стран для выпадающего списка
        List<String> countries = supplierService.getAllCountries();

        // Добавляем в модель
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("countries", countries);
        model.addAttribute("filter", filter);

        return "suppliers/list";
    }

    // Страница создания нового поставщика
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    // Сохранение нового поставщика
    @PostMapping
    public String createSupplier(@ModelAttribute Supplier supplier) {
        supplierService.createSupplier(supplier);
        return "redirect:/suppliers";
    }

    // Страница редактирования поставщика
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        model.addAttribute("supplier", supplier);
        return "suppliers/form";
    }

    // Обновление поставщика
    @PostMapping("/update/{id}")
    public String updateSupplier(@PathVariable Long id, @ModelAttribute Supplier supplier) {
        supplierService.updateSupplier(id, supplier);
        return "redirect:/suppliers";
    }

    // Удаление поставщика
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "redirect:/suppliers";
    }
}
