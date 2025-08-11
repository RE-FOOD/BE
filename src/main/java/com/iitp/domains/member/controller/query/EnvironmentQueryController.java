package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.EnvironmentResponseDto;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/environment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "환경 데이터", description = "환경 조회 API")
public class EnvironmentQueryController {
    private final MemberQueryService memberQueryService;

    @Operation(summary = "환경 정보 조회",
            description = "현재 로그인한 회원의 환경 레벨, 환경점수, 주문횟수, 다회용기 이용횟수를 조회합니다.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EnvironmentResponseDto> getCurrentEnvironmentLevel() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        log.info("현재 환경 레벨 조회 - memberId: {}", memberId);

        Member member = memberQueryService.findMemberById(memberId);
        EnvironmentResponseDto response = EnvironmentResponseDto.from(member);

        return ApiResponse.ok(200, response, "환경 정보 조회 성공");
    }
}
