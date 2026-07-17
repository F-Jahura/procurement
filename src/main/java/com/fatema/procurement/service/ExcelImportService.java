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
import java.util.Optional;

@Service
public class ExcelImportService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    // ====== ИМПОРТ ПОСТАВЩИКОВ ======
    public ImportResultDTO importSuppliers(MultipartFile file) {
        ImportResultDTO result = new ImportResultDTO();
        List<Supplier> suppliersToSave = new ArrayList<>();
        int updatedCount = 0;
        int addedCount = 0;
        int skippedCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addError("Файл не содержит листов");
                return result;
            }

            // Определяем заголовки
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addError("Файл не содержит заголовков");
                return result;
            }

            // Определяем индексы колонок
            int idIndex = -1;
            int nameIndex = -1;
            int countryIndex = -1;
            int contactIndex = -1;
            int emailIndex = -1;
            int phoneIndex = -1;
            int addressIndex = -1;
            int statusIndex = -1;
            int createdAtIndex = -1;  // ← НОВАЯ КОЛОНКА

            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                String header = getCellValue(headerRow.getCell(i));
                if (header == null) continue;

                String headerLower = header.toLowerCase().trim();
                if (headerLower.equals("id") || headerLower.equals("№")) {
                    idIndex = i;
                } else if (headerLower.equals("название") || headerLower.equals("name") || headerLower.equals("наименование")) {
                    nameIndex = i;
                } else if (headerLower.equals("страна") || headerLower.equals("country")) {
                    countryIndex = i;
                } else if (headerLower.equals("контактное лицо") || headerLower.equals("контакт") || headerLower.equals("contact person")) {
                    contactIndex = i;
                } else if (headerLower.equals("email") || headerLower.equals("e-mail")) {
                    emailIndex = i;
                } else if (headerLower.equals("телефон") || headerLower.equals("phone") || headerLower.equals("phone number")) {
                    phoneIndex = i;
                } else if (headerLower.equals("адрес") || headerLower.equals("address")) {
                    addressIndex = i;
                } else if (headerLower.equals("статус") || headerLower.equals("status")) {
                    statusIndex = i;
                } else if (headerLower.equals("дата создания") || headerLower.equals("created at") || headerLower.equals("created")) {
                    createdAtIndex = i;
                }
            }

            if (nameIndex == -1) {
                result.addError("Не найдена колонка 'Название'");
                return result;
            }

            result.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);

            for (int rowIndex = 1; rowIndex <= sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    String idStr = idIndex != -1 ? getCellValue(row.getCell(idIndex)) : null;
                    String name = nameIndex != -1 ? getCellValue(row.getCell(nameIndex)) : null;
                    String country = countryIndex != -1 ? getCellValue(row.getCell(countryIndex)) : null;
                    String contactPerson = contactIndex != -1 ? getCellValue(row.getCell(contactIndex)) : null;
                    String email = emailIndex != -1 ? getCellValue(row.getCell(emailIndex)) : null;
                    String phone = phoneIndex != -1 ? getCellValue(row.getCell(phoneIndex)) : null;
                    String address = addressIndex != -1 ? getCellValue(row.getCell(addressIndex)) : null;
                    String statusStr = statusIndex != -1 ? getCellValue(row.getCell(statusIndex)) : null;
                    String createdAtStr = createdAtIndex != -1 ? getCellValue(row.getCell(createdAtIndex)) : null;

                    if (name == null || name.trim().isEmpty()) {
                        result.addError("Строка " + (rowIndex + 1) + ": Название поставщика обязательно");
                        continue;
                    }

                    String nameTrim = name.trim();
                    String countryTrim = country != null ? country.trim() : "";
                    String contactTrim = contactPerson != null ? contactPerson.trim() : "";
                    String emailTrim = email != null ? email.trim() : "";
                    String phoneTrim = phone != null ? phone.trim() : "";
                    String addressTrim = address != null ? address.trim() : "";

                    boolean isActive = true;
                    if (statusStr != null) {
                        String status = statusStr.trim().toLowerCase();
                        if (status.equals("неактивен") || status.equals("неактивный") ||
                                status.equals("no") || status.equals("false") || status.equals("0") ||
                                status.equals("inactive")) {
                            isActive = false;
                        }
                    }

                    // ====== ПОИСК СУЩЕСТВУЮЩЕГО ПОСТАВЩИКА ======
                    Optional<Supplier> existingSupplier = Optional.empty();
                    Long existingId = null;

                    if (idStr != null && !idStr.trim().isEmpty()) {
                        try {
                            String cleanId = idStr.trim().replace(".0", "");
                            existingId = Long.parseLong(cleanId);
                            existingSupplier = supplierRepository.findById(existingId);
                        } catch (NumberFormatException e) {
                            // ID не является числом, игнорируем
                        }
                    }

                    if (existingSupplier.isEmpty()) {
                        existingSupplier = supplierRepository
                                .findByNameIgnoreCaseAndCountryIgnoreCase(nameTrim, countryTrim);
                    }

                    if (existingSupplier.isEmpty() && !emailTrim.isEmpty()) {
                        existingSupplier = supplierRepository.findByEmailIgnoreCase(emailTrim);
                    }

                    if (existingSupplier.isPresent()) {
                        Supplier supplier = existingSupplier.get();
                        boolean hasChanges = false;

                        if (!supplier.getName().equals(nameTrim)) {
                            supplier.setName(nameTrim);
                            hasChanges = true;
                        }

                        if (!supplier.getCountry().equals(countryTrim)) {
                            supplier.setCountry(countryTrim);
                            hasChanges = true;
                        }

                        if (!contactTrim.isEmpty() && !contactTrim.equals(supplier.getContactPerson())) {
                            supplier.setContactPerson(contactTrim);
                            hasChanges = true;
                        }

                        if (!emailTrim.isEmpty() && !emailTrim.equals(supplier.getEmail())) {
                            if (supplierRepository.existsByEmailAndIdNot(emailTrim, supplier.getId())) {
                                result.addError("Строка " + (rowIndex + 1) + ": Email '" + emailTrim +
                                        "' уже используется другим поставщиком");
                                continue;
                            }
                            supplier.setEmail(emailTrim);
                            hasChanges = true;
                        }

                        if (!phoneTrim.isEmpty() && !phoneTrim.equals(supplier.getPhone())) {
                            supplier.setPhone(phoneTrim);
                            hasChanges = true;
                        }

                        if (!addressTrim.isEmpty() && !addressTrim.equals(supplier.getAddress())) {
                            supplier.setAddress(addressTrim);
                            hasChanges = true;
                        }

                        if (supplier.isActive() != isActive) {
                            supplier.setActive(isActive);
                            hasChanges = true;
                        }

                        if (hasChanges) {
                            suppliersToSave.add(supplier);
                            updatedCount++;
                            result.addSuccess("Строка " + (rowIndex + 1) + ": Поставщик '" + nameTrim +
                                    "' (ID: " + supplier.getId() + ") обновлён");
                        } else {
                            skippedCount++;
                            result.addSuccess("Строка " + (rowIndex + 1) + ": Поставщик '" + nameTrim +
                                    "' (ID: " + supplier.getId() + ") без изменений");
                        }

                    } else {
                        // ====== СОЗДАЁМ НОВОГО ======

                        if (!emailTrim.isEmpty() && supplierRepository.existsByEmail(emailTrim)) {
                            result.addError("Строка " + (rowIndex + 1) + ": Email '" + emailTrim +
                                    "' уже используется другим поставщиком");
                            continue;
                        }

                        Optional<Supplier> existingByNameAndCountry = supplierRepository
                                .findByNameIgnoreCaseAndCountryIgnoreCase(nameTrim, countryTrim);
                        if (existingByNameAndCountry.isPresent()) {
                            result.addError("Строка " + (rowIndex + 1) + ": Поставщик с названием '" + nameTrim +
                                    "' и страной '" + countryTrim + "' уже существует");
                            continue;
                        }

                        Supplier newSupplier = new Supplier();
                        newSupplier.setName(nameTrim);
                        newSupplier.setCountry(countryTrim);
                        newSupplier.setContactPerson(contactTrim);
                        newSupplier.setEmail(emailTrim);
                        newSupplier.setPhone(phoneTrim);
                        newSupplier.setAddress(addressTrim);
                        newSupplier.setActive(isActive);
                        // Дата создания будет установлена автоматически через @PrePersist

                        suppliersToSave.add(newSupplier);
                        addedCount++;
                        result.addSuccess("Строка " + (rowIndex + 1) + ": Поставщик '" + nameTrim +
                                "' (страна: " + countryTrim + ") добавлен");
                    }

                } catch (Exception e) {
                    result.addError("Строка " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }

            if (!suppliersToSave.isEmpty()) {
                supplierRepository.saveAll(suppliersToSave);
            }

            result.setSuccessCount(addedCount + updatedCount);
            result.setErrorCount(result.getErrors().size());

            if (skippedCount > 0) {
                result.addInfo("Пропущено (без изменений): " + skippedCount + " записей");
            }

        } catch (Exception e) {
            result.addError("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    // ====== ИМПОРТ ТОВАРОВ ======
    public ImportResultDTO importProducts(MultipartFile file) {
        ImportResultDTO result = new ImportResultDTO();
        List<Product> productsToSave = new ArrayList<>();
        int updatedCount = 0;
        int addedCount = 0;
        int skippedCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.addError("Файл не содержит листов");
                return result;
            }

            result.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);

            List<Supplier> suppliers = supplierRepository.findAll();

            for (int rowIndex = 1; rowIndex <= sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    // ====== ВАША СТРУКТУРА EXCEL ======
                    // ID | Название | Артикул (SKU) | Поставщик | Себестоимость | Продажная цена | Остаток | Мин. остаток | Статус остатка
                    // 0  |    1     |       2       |     3     |       4       |        5       |    6    |      7       |       8

                    String idStr = getCellValue(row.getCell(0));
                    String name = getCellValue(row.getCell(1));

                    // ====== ОЧИЩАЕМ SKU ОТ .0 ======
                    String sku = getCellValue(row.getCell(2));
                    if (sku != null) {
                        sku = sku.trim().replace(".0", "");
                    }

                    String supplierName = getCellValue(row.getCell(3));
                    String priceStr = getCellValue(row.getCell(4));
                    String stockStr = getCellValue(row.getCell(6));
                    String minStockStr = getCellValue(row.getCell(7));

                    if (name == null || name.trim().isEmpty()) {
                        continue;
                    }

                    String nameTrim = name.trim();
                    String skuTrim = sku != null ? sku.trim() : "";
                    String supplierNameTrim = supplierName != null ? supplierName.trim() : "";
                    String unitTrim = "шт";

                    Supplier supplier = null;
                    if (!supplierNameTrim.isEmpty()) {
                        supplier = suppliers.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(supplierNameTrim))
                                .findFirst()
                                .orElse(null);
                        if (supplier == null) {
                            result.addError("Строка " + (rowIndex + 1) + ": Поставщик '" + supplierNameTrim + "' не найден");
                            continue;
                        }
                    } else {
                        result.addError("Строка " + (rowIndex + 1) + ": Поставщик не указан");
                        continue;
                    }

                    // ====== ПОИСК СУЩЕСТВУЮЩЕГО ТОВАРА ======
                    Optional<Product> existingProduct = Optional.empty();
                    Long existingId = null;

                    if (idStr != null && !idStr.trim().isEmpty()) {
                        try {
                            existingId = Long.parseLong(idStr.trim());
                            existingProduct = productRepository.findById(existingId);
                        } catch (NumberFormatException e) {
                            // ID не является числом, игнорируем
                        }
                    }

                    if (existingProduct.isEmpty() && !skuTrim.isEmpty()) {
                        existingProduct = productRepository.findBySku(skuTrim);
                    }

                    if (existingProduct.isEmpty() && supplier != null) {
                        existingProduct = productRepository.findByNameIgnoreCaseAndSupplierId(nameTrim, supplier.getId());
                    }

                    if (existingProduct.isPresent()) {
                        // ====== ОБНОВЛЯЕМ СУЩЕСТВУЮЩИЙ ТОВАР ======
                        Product product = existingProduct.get();
                        boolean hasChanges = false;

                        // Обновляем название
                        if (!product.getName().equals(nameTrim)) {
                            product.setName(nameTrim);
                            hasChanges = true;
                        }

                        // Обновляем SKU (уже очищенный)
                        if (!skuTrim.isEmpty() && !skuTrim.equals(product.getSku())) {
                            if (productRepository.existsBySkuAndIdNot(skuTrim, product.getId())) {
                                result.addError("Строка " + (rowIndex + 1) + ": Артикул '" + skuTrim +
                                        "' уже используется другим товаром");
                                continue;
                            }
                            product.setSku(skuTrim);
                            hasChanges = true;
                        }

                        // Обновляем поставщика
                        if (supplier != null && (product.getSupplier() == null ||
                                !product.getSupplier().getId().equals(supplier.getId()))) {
                            product.setSupplier(supplier);
                            hasChanges = true;
                        }

                        // Обновляем себестоимость
                        if (priceStr != null && !priceStr.trim().isEmpty()) {
                            try {
                                BigDecimal newPrice = new BigDecimal(priceStr.trim().replace(",", "."));
                                if (product.getCostPrice() == null || !product.getCostPrice().equals(newPrice)) {
                                    product.setCostPrice(newPrice);
                                    product.calculateSellingPrice();
                                    hasChanges = true;
                                }
                            } catch (NumberFormatException e) {
                                result.addError("Строка " + (rowIndex + 1) + ": Неверный формат цены");
                                continue;
                            }
                        }

                        // ====== ОБНОВЛЯЕМ ОСТАТОК ======
                        if (stockStr != null && !stockStr.trim().isEmpty()) {
                            try {
                                String cleanStockStr = stockStr.trim().replace(".0", "").replace(",", "");
                                int newStock = Integer.parseInt(cleanStockStr);
                                if (product.getCurrentStock() == null || product.getCurrentStock() != newStock) {
                                    product.setCurrentStock(newStock);
                                    hasChanges = true;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("❌ Ошибка парсинга остатка: " + stockStr);
                            }
                        }

                        // ====== ОБНОВЛЯЕМ МИНИМАЛЬНЫЙ ОСТАТОК ======
                        if (minStockStr != null && !minStockStr.trim().isEmpty()) {
                            try {
                                String cleanMinStockStr = minStockStr.trim().replace(".0", "").replace(",", "");
                                int newMinStock = Integer.parseInt(cleanMinStockStr);
                                if (product.getMinStock() == null || product.getMinStock() != newMinStock) {
                                    product.setMinStock(newMinStock);
                                    hasChanges = true;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("❌ Ошибка парсинга мин. остатка: " + minStockStr);
                            }
                        }

                        if (hasChanges) {
                            productsToSave.add(product);
                            updatedCount++;
                            result.addSuccess("Строка " + (rowIndex + 1) + ": Товар '" + nameTrim +
                                    "' (ID: " + product.getId() + ") обновлён");
                        } else {
                            skippedCount++;
                            result.addSuccess("Строка " + (rowIndex + 1) + ": Товар '" + nameTrim +
                                    "' (ID: " + product.getId() + ") без изменений");
                        }

                    } else {
                        // ====== СОЗДАЁМ НОВЫЙ ТОВАР ======

                        if (!skuTrim.isEmpty() && productRepository.existsBySku(skuTrim)) {
                            result.addError("Строка " + (rowIndex + 1) + ": Артикул '" + skuTrim + "' уже существует");
                            continue;
                        }

                        if (supplier != null && productRepository.existsByNameIgnoreCaseAndSupplierId(nameTrim, supplier.getId())) {
                            result.addError("Строка " + (rowIndex + 1) + ": Товар с названием '" + nameTrim +
                                    "' уже существует у поставщика '" + supplier.getName() + "'");
                            continue;
                        }

                        Product newProduct = new Product();
                        newProduct.setName(nameTrim);
                        newProduct.setSku(skuTrim);  // SKU уже очищен
                        newProduct.setSupplier(supplier);
                        newProduct.setUnit(unitTrim);

                        // Устанавливаем себестоимость
                        if (priceStr != null && !priceStr.trim().isEmpty()) {
                            try {
                                newProduct.setCostPrice(new BigDecimal(priceStr.trim().replace(",", ".")));
                            } catch (NumberFormatException e) {
                                newProduct.setCostPrice(BigDecimal.ZERO);
                            }
                        } else {
                            newProduct.setCostPrice(BigDecimal.ZERO);
                        }
                        newProduct.calculateSellingPrice();

                        // Устанавливаем остаток
                        if (stockStr != null && !stockStr.trim().isEmpty()) {
                            try {
                                String cleanStockStr = stockStr.trim().replace(".0", "").replace(",", "");
                                newProduct.setCurrentStock(Integer.parseInt(cleanStockStr));
                            } catch (NumberFormatException e) {
                                newProduct.setCurrentStock(0);
                            }
                        } else {
                            newProduct.setCurrentStock(0);
                        }

                        // Устанавливаем минимальный остаток
                        if (minStockStr != null && !minStockStr.trim().isEmpty()) {
                            try {
                                String cleanMinStockStr = minStockStr.trim().replace(".0", "").replace(",", "");
                                newProduct.setMinStock(Integer.parseInt(cleanMinStockStr));
                            } catch (NumberFormatException e) {
                                newProduct.setMinStock(0);
                            }
                        } else {
                            newProduct.setMinStock(0);
                        }

                        productsToSave.add(newProduct);
                        addedCount++;
                        result.addSuccess("Строка " + (rowIndex + 1) + ": Товар '" + nameTrim + "' добавлен");
                    }

                } catch (Exception e) {
                    result.addError("Строка " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }

            if (!productsToSave.isEmpty()) {
                productRepository.saveAll(productsToSave);
            }

            result.setSuccessCount(addedCount + updatedCount);
            result.setErrorCount(result.getErrors().size());

            if (skippedCount > 0) {
                result.addInfo("Пропущено (без изменений): " + skippedCount + " записей");
            }

        } catch (Exception e) {
            result.addError("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    // ====== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ СОЗДАНИЯ НОВОГО ТОВАРА ======
    private Product createNewProduct(String name, String sku, Supplier supplier, String unit, String priceStr, String stockStr) {
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setSku(sku);
        newProduct.setSupplier(supplier);
        newProduct.setUnit(unit);
        newProduct.setMinStock(0);

        if (priceStr != null && !priceStr.trim().isEmpty()) {
            try {
                newProduct.setCostPrice(new BigDecimal(priceStr.trim().replace(",", ".")));
            } catch (NumberFormatException e) {
                newProduct.setCostPrice(BigDecimal.ZERO);
            }
        } else {
            newProduct.setCostPrice(BigDecimal.ZERO);
        }
        newProduct.calculateSellingPrice();

        if (stockStr != null && !stockStr.trim().isEmpty()) {
            try {
                newProduct.setCurrentStock(Integer.parseInt(stockStr.trim()));
            } catch (NumberFormatException e) {
                newProduct.setCurrentStock(0);
            }
        } else {
            newProduct.setCurrentStock(0);
        }

        return newProduct;
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
