package com.fatema.procurement.controller;

import com.fatema.procurement.dto.ImportResultDTO;
import com.fatema.procurement.dto.SupplierFilterDTO;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.SupplierRepository;
import com.fatema.procurement.service.ExcelExportService;
import com.fatema.procurement.service.ExcelImportService;
import com.fatema.procurement.service.SupplierService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierControllerUI {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ExcelImportService excelImportService;

    @Autowired
    private ExcelExportService excelExportService;
    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping("/export/excel")
    public void exportSuppliers(HttpServletResponse response) throws IOException {
        List<Supplier> suppliers = supplierRepository.findAll(); // или через сервис
        excelExportService.exportSuppliers(suppliers, response);
    }


    // Страница со списком поставщиков с фильтрацией
    @GetMapping
    public String listSuppliers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean active,
            Model model) {

        // Создаём объект фильтра
        //SupplierFilterDTO filter = new SupplierFilterDTO(name, country, active);
        SupplierFilterDTO filter = new SupplierFilterDTO();
        filter.setName(name);
        filter.setCountry(country);
        filter.setActive(active);

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

    @PostMapping("/import")
    public String importSuppliers(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        ImportResultDTO result = excelImportService.importSuppliers(file);
        redirectAttributes.addFlashAttribute("importResult", result);
        return "redirect:/suppliers";
    }
}
