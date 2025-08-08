package com.iitp.domains.store.dto.request;

import com.iitp.domains.store.domain.Category;

import java.sql.Timestamp;
import java.util.List;

public record StoreUpdateRequest(
        String name,
        String phoneNumber,
        String address,
        String description,
        String origin,
        Timestamp openTime,
        Timestamp closeTime,
        Category category
) {
}

