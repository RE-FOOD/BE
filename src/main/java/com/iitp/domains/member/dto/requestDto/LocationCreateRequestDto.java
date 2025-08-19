package com.iitp.domains.member.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LocationCreateRequestDto(
        @NotBlank(message = "주소는 필수입니다.")
        String address
) {
}
