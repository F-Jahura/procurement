package com.fatema.procurement.service;

import com.fatema.procurement.dto.ImportResultDTO;
import com.fatema.procurement.entity.Product;
import com.fatema.procurement.entity.Supplier;
import com.fatema.procurement.repository.ProductRepository;
import com.fatema.procurement.repository.SupplierRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelImportService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    public ImportResultDTO importSuppliers(MultipartFile file) {
        ImportResultDTO result = new ImportResultDTO();
        List<Supplier> suppliers = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            result.setTotalRows(sheet.getPhysicalNumberOfRows() - 1); // минус заголовок

            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    String name = getCellValue(row.getCell(0));
                    String country = getCellValue(row.getCell(1));
                    String contactPerson = getCellValue(row.getCell(2));
                    String email = getCellValue(row.getCell(3));
                    String phone = getCellValue(row.getCell(4));
                    String address = getCellValue(row.getCell(5));

                    if (name == null || name.trim().isEmpty()) {
                        result.addError("Строка " + (rowIndex + 1) + ": Название поставщика обязательно");
                        continue;
                    }

                    Supplier supplier = new Supplier();
                    supplier.setName(name.trim());
                    supplier.setCountry(country != null ? country.trim() : "");
                    supplier.setContactPerson(contactPerson != null ? contactPerson.trim() : "");
                    supplier.setEmail(email != null ? email.trim() : "");
                    supplier.setPhone(phone != null ? phone.trim() : "");
                    supplier.setAddress(address != null ? address.trim() : "");
                    supplier.setActive(true);

                    suppliers.add(supplier);
                    result.addSuccess("Поставщик '" + name + "' добавлен");

                } catch (Exception e) {
                    result.addError("Строка " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }

            if (!suppliers.isEmpty()) {
                supplierRepository.saveAll(suppliers);
            }

        } catch (Exception e) {
            result.addError("Ошибка при чтении файла: " + e.getMessage());
        }

        return result;
    }

    public ImportResultDTO importProducts(MultipartFile file) {
        ImportResultDTO result = new ImportResultDTO();
        List<Product> products = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            result.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);

            // Получаем всех поставщиков для поиска
            List<Supplier> suppliers = supplierRepository.findAll();

            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    String name = getCellValue(row.getCell(0));
                    String sku = getCellValue(row.getCell(1));
                    String supplierName = getCellValue(row.getCell(2));
                    String priceStr = getCellValue(row.getCell(3));
                    String stockStr = getCellValue(row.getCell(4));
                    String unit = getCellValue(row.getCell(5));

                    if (name == null || name.trim().isEmpty()) {
                        result.addError("Строка " + (rowIndex + 1) + ": Название товара обязательно");
                        continue;
                    }

                    // Ищем поставщика по названию
                    Supplier supplier = null;
                    if (supplierName != null && !supplierName.trim().isEmpty()) {
                        supplier = suppliers.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(supplierName.trim()))
                                .findFirst()
                                .orElse(null);
                    }

                    Product product = new Product();
                    product.setName(name.trim());
                    product.setSku(sku != null ? sku.trim() : "");
                    product.setSupplier(supplier);

                    if (priceStr != null && !priceStr.trim().isEmpty()) {
                        try {
                            product.setPrice(new BigDecimal(priceStr.trim().replace(",", ".")));
                        } catch (NumberFormatException e) {
                            result.addError("Строка " + (rowIndex + 1) + ": Неверный формат цены");
                            continue;
                        }
                    } else {
                        product.setPrice(BigDecimal.ZERO);
                    }

                    if (stockStr != null && !stockStr.trim().isEmpty()) {
                        try {
                            product.setCurrentStock(Integer.parseInt(stockStr.trim()));
                        } catch (NumberFormatException e) {
                            product.setCurrentStock(0);
                        }
                    } else {
                        product.setCurrentStock(0);
                    }

                    product.setUnit(unit != null ? unit.trim() : "шт");
                    product.setMinStock(0);

                    products.add(product);
                    result.addSuccess("Товар '" + name + "' добавлен");

                } catch (Exception e) {
                    result.addError("Строка " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }

            if (!products.isEmpty()) {
                productRepository.saveAll(products);
            }

        } catch (Exception e) {
            result.addError("Ошибка при чтении файла: " + e.getMessage());
        }

        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}
