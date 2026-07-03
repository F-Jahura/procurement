package com.fatema.procurement.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<String> errors = new ArrayList<>();
    private List<String> messages = new ArrayList<>();

    public ImportResultDTO() {}

    public void addSuccess(String message) {
        successCount++;
        messages.add("✅ " + message);
    }

    public void addError(String error) {
        errorCount++;
        errors.add("❌ " + error);
    }

    // Геттеры и сеттеры
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }

    public boolean hasErrors() { return errorCount > 0; }
}
