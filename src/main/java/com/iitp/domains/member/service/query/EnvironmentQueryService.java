package com.iitp.domains.member.service.query;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.EnvironmentReportResponseDto;
import com.iitp.domains.member.dto.responseDto.EnvironmentResponseDto;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.member.repository.MemberRepositoryImpl;
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
    private final MemberRepository memberRepository;
    private final MemberRepositoryImpl memberRepositoryImpl;


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

    public EnvironmentReportResponseDto getTotalEnvironmentReport() {
        log.debug("전체 환경 리포트 조회 시작");

        // 전체 주문 횟수 조회
        Integer totalOrderCount = memberRepository.sumAllOrderCount();
        if (totalOrderCount == null) {
            totalOrderCount = 0;
        }

        // 전체 다회용기 사용 횟수 조회
        Integer totalDishCount = memberRepository.sumAllDishCount();
        if (totalDishCount == null) {
            totalDishCount = 0;
        }

        log.info("전체 환경 리포트 - 픽업주문: {}회, 다회용기: {}회",
                totalOrderCount, totalDishCount);

        // DTO 생성 및 환경 기여도 계산
        return EnvironmentReportResponseDto.from(totalOrderCount, totalDishCount);
    }

}
