package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.dto.responseDto.MyPageResponseDto;
import com.iitp.domains.member.service.query.MypageQueryService;
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
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "마이페이지", description = "마이페이지 조회 API")
public class MypageQueryController {
    private final MypageQueryService myPageQueryService;

    @Operation(summary = "마이페이지 메인 조회",
            description = "현재 로그인한 회원의 마이페이지 정보를 조회합니다." +
                    "환경점수는 주후 도입 예정입니다.")
    @GetMapping("/main")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MyPageResponseDto> getMyPage() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        log.info("마이페이지 조회 - memberId: {}", memberId);

        MyPageResponseDto response = myPageQueryService.getMyPageInfo(memberId);

        return ApiResponse.ok(200, response, "마이페이지 조회 성공");
    }
}
