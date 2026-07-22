package com.fatema.procurement.entity;

public enum ReceiptStatus {
    DRAFT("Черновик"),
    CONFIRMED("Оприходован"),
    CANCELLED("Отменён"),
    PARTIAL("Частичный приход");

    private final String displayName;

    ReceiptStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}