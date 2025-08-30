package com.iitp.domains.member.service;

import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.domains.payment.dto.PaymentRewardDto;
import com.iitp.domains.payment.dto.response.PaymentConfirmResponse;
import com.iitp.global.common.constants.BusinessLogicConstants;
import com.iitp.global.util.environment.EnvironmentPointCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 환경 포인트 지급 서비스
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EnvironmentRewardService {

    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;

    /**
     * 주문 완료시 환경 포인트 지급 및 통계 업데이트
     *
     * @param memberId 회원 ID
     * @param orderAmount 주문 금액
     * @param isContainerReused 다회용기 사용 여부
     */
    public PaymentRewardDto processOrderEnvironmentReward(Long memberId, int orderAmount, boolean isContainerReused) {
        log.info("환경 포인트 지급 시작 - memberId: {}, orderAmount: {}, containerReused: {}",
                memberId, orderAmount, isContainerReused);

        // 회원 조회
        Member member = memberQueryService.findMemberById(memberId);

        // 비교 기준 포인트 값
        int equalsPoint = 0;
        int point = member.getEnvironmentPoint();

        if(member.getEnvironmentPoint() >= 2400){
            equalsPoint = 5600;
        }else if(member.getEnvironmentPoint() >= 800){
            equalsPoint = 2400;
        }else{
            equalsPoint = 800;
        }

        // 환경 포인트 계산 및 지급
        int environmentPoint = EnvironmentPointCalculator.calculateTotalEnvironmentPoint(orderAmount, isContainerReused);
        member.addEnvironmentPoint(environmentPoint);

        // 주문 횟수 증가
        member.incrementOrderCount();

        // 다회용기 사용시 횟수 증가
        if (isContainerReused) {
            member.incrementDishCount();
        }


        log.info("환경 포인트 지급 완료 - memberId: {}, 지급포인트: {}, 총포인트: {}, 레벨: {}",
                memberId, environmentPoint, member.getEnvironmentPoint(), member.getEnvironmentLevel());

        PaymentRewardDto response = null;



        if((member.getEnvironmentPoint() >= equalsPoint) && (point <= 5600)) {
            return response = PaymentRewardDto.builder()
                    .levelCheck(true)
                    .level(member.getEnvironmentLevel())
                    .build();
        }else{
            return response = PaymentRewardDto.builder()
                    .levelCheck(false)
                    .level(member.getEnvironmentLevel())
                    .build();
        }

    }

    /**
     * 단순한 주문 완료 처리 (다회용기 사용 안함)
     *
     * @param memberId 회원 ID
     * @param orderAmount 주문 금액
     */
    public void processOrderEnvironmentReward(Long memberId, int orderAmount) {
        processOrderEnvironmentReward(memberId, orderAmount, false);
    }
}
