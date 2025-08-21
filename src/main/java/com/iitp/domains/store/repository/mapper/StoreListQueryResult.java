package com.iitp.domains.store.repository.mapper;

import com.iitp.domains.store.domain.StoreStatus;

import java.time.LocalTime;
import java.util.List;

public record StoreListQueryResult(
        Long id,
        String name,
        StoreStatus status,
        String imageKey,
        int maxPercent,
        LocalTime openTime,
        LocalTime closeTime
) {
}
