package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.dto.responseDto.MemberProfileResponseDto;
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
}
