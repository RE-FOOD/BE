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
        Double ratingAvg,                   // 리뷰 평균 평점
        long count,                          // 리뷰 총 개수
        LocalTime openTime,
        LocalTime closeTime,
        double distance
) {
}
