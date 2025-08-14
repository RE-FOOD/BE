package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.EnvironmentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EnvironmentQueryService {

    private final MemberQueryService memberQueryService;

    /**
     * 환경 정보 조회
     */
    @Cacheable(value = "environment", key = "'member:' + #memberId")
    public EnvironmentResponseDto getEnvironmentInfo(Long memberId) {
        log.debug("환경 정보 조회 시작 - memberId: {}", memberId);

        // 1. 회원 정보 조회 (MemberQueryService 위임)
        Member member = memberQueryService.findMemberById(memberId);

        log.debug("환경 정보 조회 완료 - memberId: {}, level: {}, point: {}, orderCount: {}, dishCount: {}",
                memberId, member.getEnvironmentLevel(), member.getEnvironmentPoint(),
                member.getOrderCount(), member.getDishCount());

        // 2. EnvironmentResponseDto 생성
        return EnvironmentResponseDto.from(member);
    }
}
