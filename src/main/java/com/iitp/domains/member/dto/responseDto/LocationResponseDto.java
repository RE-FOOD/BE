package com.iitp.domains.member.dto.responseDto;

import lombok.Builder;

@Builder
public record LocationResponseDto(
        Long id,
        String address,
        Boolean isMostRecent
) {
    public static LocationResponseDto of(Long id, String address, Boolean isMostRecent) {
        return LocationResponseDto.builder()
                .id(id)
                .address(address)
                .isMostRecent(isMostRecent)
                .build();
    }
}
