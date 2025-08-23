package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.entity.Location;
import lombok.Builder;

@Builder
public record LocationResponseDto(
        Long id,
        String address,
        String roadAddress,
        Double latitude, // 위도
        Double longitude, // 경도
        Boolean isMostRecent
) {
    public static LocationResponseDto from(Location location) {
        if (location == null) {
            return null;
        }

        return LocationResponseDto.builder()
                .id(location.getId())
                .address(location.getAddress())
                .roadAddress(location.getRoadAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .isMostRecent(location.getIsMostRecent())
                .build();
    }
}
