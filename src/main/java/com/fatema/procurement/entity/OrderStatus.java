package com.fatema.procurement.entity;

public enum OrderStatus {
    DRAFT("Черновик"),
    CONFIRMED("Подтверждён"),
    SENT("Отправлен"),
    SHIPPED("Отгружен"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменён");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
