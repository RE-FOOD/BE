package com.iitp.domains.member.dto.responseDto;

import lombok.Builder;

@Builder
public record LocationResponseDto(
        Long id,

        String address,

        Boolean isMostRecent
) {
}
