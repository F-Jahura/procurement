package com.fatema.procurement.service;

import com.fatema.procurement.entity.Product;
import com.fatema.procurement.entity.Supplier;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public void exportSuppliers(List<Supplier> suppliers, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Поставщики");

        // Заголовки
        String[] columns = {"ID", "Название", "Страна", "Контактное лицо", "Email", "Телефон", "Адрес", "Статус"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        // Данные
        int rowNum = 1;
        for (Supplier supplier : suppliers) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(supplier.getId() != null ? supplier.getId() : 0);
            row.createCell(1).setCellValue(supplier.getName() != null ? supplier.getName() : "");
            row.createCell(2).setCellValue(supplier.getCountry() != null ? supplier.getCountry() : "");
            row.createCell(3).setCellValue(supplier.getContactPerson() != null ? supplier.getContactPerson() : "");
            row.createCell(4).setCellValue(supplier.getEmail() != null ? supplier.getEmail() : "");
            row.createCell(5).setCellValue(supplier.getPhone() != null ? supplier.getPhone() : "");
            row.createCell(6).setCellValue(supplier.getAddress() != null ? supplier.getAddress() : "");
            row.createCell(7).setCellValue(supplier.isActive() ? "Активен" : "Неактивен");
        }

        // Автоширина
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        sendResponse(workbook, response, "suppliers.xlsx");
    }

    public void exportProducts(List<Product> products, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Товары");

        // Заголовки (БЕЗ колонки "Продажи")
        String[] columns = {"ID", "Название", "Артикул", "Поставщик", "Цена", "Остаток", "Ед. изм."};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        // Данные
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getId() != null ? product.getId() : 0);
            row.createCell(1).setCellValue(product.getName() != null ? product.getName() : "");
            row.createCell(2).setCellValue(product.getSku() != null ? product.getSku() : "");
            row.createCell(3).setCellValue(product.getSupplier() != null ? product.getSupplier().getName() : "");
            row.createCell(4).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
            row.createCell(5).setCellValue(product.getCurrentStock() != null ? product.getCurrentStock() : 0);
            row.createCell(6).setCellValue(product.getUnit() != null ? product.getUnit() : "шт");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        sendResponse(workbook, response, "products.xlsx");
    }


    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void sendResponse(Workbook workbook, HttpServletResponse response, String filename) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
