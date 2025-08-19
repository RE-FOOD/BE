package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.entity.Location;
import lombok.Builder;

@Builder
public record LocationResponseDto(
        Long id,
        String address,
        Boolean isMostRecent
) {
    public static LocationResponseDto from(Location location) {
        if (location == null) {
            return null;
        }

        return LocationResponseDto.builder()
                .id(location.getId())
                .address(location.getAddress())
                .isMostRecent(location.getIsMostRecent())
                .build();
    }
}
