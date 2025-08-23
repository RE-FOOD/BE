package com.iitp.domains.store.dto.request;

public record MenuUpdateRequest (
        String name,
        String info,
        int dailyQuantity,
        String imageKey
){
}
