package com.iitp.domains.store.dto.request;

import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.domain.entity.Store;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;

public record StoreCreateRequest (
        String name,
        String phoneNumber,
        String address,
        String description,
        String origin,
        Timestamp openTime,
        Timestamp closeTime,
        Category category,
        List<String> imageKey
){

    public Store toEntity(Long memberId, double lat, double lon) {
        return Store.builder()
                .name(this.name)
                .memberId(memberId)
                .phoneNumber(this.phoneNumber)
                .status(StoreStatus.OPEN)
                .address(this.address)
                .latitude(lat)
                .longitude(lon)
                .description(this.description)
                .origin(this.origin)
                .openTime(LocalTime.from(this.openTime.toLocalDateTime()))
                .closeTime(LocalTime.from(this.closeTime.toLocalDateTime()))
                .category(this.category)
                .build();
    }
}
