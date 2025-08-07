package com.iitp.domains.store.domain;

public enum Category {
    ENFOOD("양식"),
    CHFOOD("중식"),
    KRFOOD("한식"),
    JPFOOD("일식"),
    SNACKFOOD("분식"),
    DESSERT("디저트");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
