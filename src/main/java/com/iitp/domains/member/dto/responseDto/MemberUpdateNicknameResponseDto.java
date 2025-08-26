package com.iitp.domains.member.dto.responseDto;

import com.iitp.domains.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MemberUpdateNicknameResponseDto(
        Long id,
        String nickname
) {
    /**
     * Member 엔티티로부터 생성
     */
    public static MemberUpdateNicknameResponseDto from(Member member) {
        return MemberUpdateNicknameResponseDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .build();
    }
}
