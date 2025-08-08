package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.service.query.MemberQueryService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 조회", description = "회원 조회 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/nickname")
    public ApiResponse<Boolean> checkNickname(
            @Parameter(description = "확인할 닉네임")
            @RequestParam String nickname){
        boolean available =! memberQueryService.isNicknameExists(nickname);
        return ApiResponse.ok(200, available, available?"사용가능": "이미 사용중");
    }

    @Operation(summary = "전화번호 중복 확인", description = "전화번호 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/phone")
    public ApiResponse<Boolean> checkPhone(
            @Parameter(description = "확인할 전화번호", example = "01012345678")
            @RequestParam String phone) {
        boolean available = !memberQueryService.isPhoneExists(phone);
        return ApiResponse.ok(200, available, available ? "사용 가능" : "이미 사용중");
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/email")
    public ApiResponse<Boolean> checkEmail(
            @Parameter(description = "확인할 이메일", example = "test@example.com")
            @RequestParam String email) {
        boolean available = !memberQueryService.isEmailExists(email);
        return ApiResponse.ok(200, available, available ? "사용 가능" : "이미 사용중");
    }

    @Operation(summary = "사업자 번호 중복 확인", description = "사업자 번호 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/businessLicense")
    public ApiResponse<Boolean> checkBusinessLicense(
            @Parameter(description = "확인할 사업자 번호", example = "123-45-67890")
            @RequestParam String businessLicenseNumber) {
        boolean available = !memberQueryService.isBusinessLicenseNumberExists(businessLicenseNumber);
        return ApiResponse.ok(200, available, available ? "사용 가능한 사업자 번호입니다" : "이미 등록된 사업자 번호입니다");
    }



}
