package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.MyPageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MypageQueryService {

    private final MemberQueryService memberQueryService;

    /**
     * 마이페이지 정보 조회
     */
    @Cacheable(value = "mypage", key = "'member:' + #memberId")
    public MyPageResponseDto getMyPageInfo(Long memberId) {
        log.debug("마이페이지 정보 조회 시작 - memberId: {}", memberId);

        // 1. 회원 정보 조회 (MemberQueryService 위임)
        Member member = memberQueryService.findMemberById(memberId);

        log.debug("마이페이지 정보 조회 완료 - memberId: {}, orderCount: {}, dishCount: {}, member.getEnvironmentPoint: {}",
                memberId, member.getOrderCount(), member.getDishCount(), member.getEnvironmentPoint());

        // 2. MyPageResponseDto 생성
        return MyPageResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .environmentLevel(member.getEnvironmentLevel())
                .orderCount(member.getOrderCount())
                .dishCount(member.getDishCount())
                .environmentScore(member.getEnvironmentPoint())
                .build();
    }
}
