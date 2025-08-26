package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.dto.responseDto.MainOverviewResponseDto;
import com.iitp.domains.member.dto.responseDto.MemberProfileResponseDto;
import com.iitp.domains.member.service.MainOverviewService;
import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.config.security.SecurityUtil;
import com.iitp.global.exception.ConflictException;
import com.iitp.global.exception.ExceptionMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 조회", description = "회원 조회 API")
@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
@Slf4j
public class MemberQueryController {

    private final MemberQueryService memberQueryService;
    private final MainOverviewService mainOverviewService;

    @Operation(summary = "내 프로필 조회",
            description = "현재 로그인한 회원의 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MemberProfileResponseDto> getMyProfile() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        log.info("회원 프로필 조회 - memberId: {}", memberId);

        MemberProfileResponseDto response = memberQueryService.getMemberProfile(memberId);

        return ApiResponse.ok(200, response, "프로필 조회 성공");
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/nickname")
    public ApiResponse<String> checkNickname(
            @Parameter(description = "확인할 닉네임")
            @RequestParam String nickname){
        if (memberQueryService.isNicknameExists(nickname)) {
            // 중복인 경우 409 Conflict 예외 발생
            throw new ConflictException(ExceptionMessage.NICKNAME_ALREADY_EXISTS);
        }
        return ApiResponse.okWithoutData(200, "사용가능한 닉네임 입니다.");
    }

    @Operation(
            summary = "메인 페이지 데이터 호출",
            description = "현재 로그인한 회원의 메인 페이지에 필요한 모든 데이터를 조회합니다. " +
                    "장바구니 개수, 알림 존재 여부, 기본 주소, 할인 메뉴 목록, 인기 가게 목록을 포함합니다."
    )
    @GetMapping("/me/overviews")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MainOverviewResponseDto> getMainOverview() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        log.info("메인 페이지 데이터 조회 요청 - memberId: {}", memberId);

        MainOverviewResponseDto response = mainOverviewService.getMainOverview(memberId);

        log.info("메인 페이지 데이터 조회 완료 - memberId: {}", memberId);

        return ApiResponse.ok(200, response, "메인 데이터 호출 성공");
    }
}
