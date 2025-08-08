package com.iitp.domains.store.domain;

public enum StoreStatus {
    OPEN("가게 오픈"),
    CLOSED("가게 마감");

    private final String description;

    StoreStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
