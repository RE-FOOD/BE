package com.iitp.domains.member.controller.query;

import com.iitp.domains.member.service.query.MemberReadService;
import com.iitp.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 조회", description = "회원 조회 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class GetMemberController {

    private final MemberReadService memberReadService;

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(
            @Parameter(description = "확인할 닉네임")
            @RequestParam String nickname){
        boolean available =! memberReadService.isNicknameExists(nickname);
        return ResponseEntity.ok(ApiResponse.ok(available, available?"사용가능": "이미 사용중"));
    }

    @Operation(summary = "전화번호 중복 확인", description = "전화번호 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/phone")
    public ResponseEntity<ApiResponse<Boolean>> checkPhone(
            @Parameter(description = "확인할 전화번호", example = "01012345678")
            @RequestParam String phone) {
        boolean available = !memberReadService.isPhoneExists(phone);
        return ResponseEntity.ok(ApiResponse.ok(available, available ? "사용 가능" : "이미 사용중"));
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(
            @Parameter(description = "확인할 이메일", example = "test@example.com")
            @RequestParam String email) {
        boolean available = !memberReadService.isEmailExists(email);
        return ResponseEntity.ok(ApiResponse.ok(available, available ? "사용 가능" : "이미 사용중"));
    }

    @Operation(summary = "사업자 번호 중복 확인", description = "사업자 번호 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/businessLicense")
    public ResponseEntity<ApiResponse<Boolean>> checkBusinessLicense(
            @Parameter(description = "확인할 사업자 번호", example = "123-45-67890")
            @RequestParam String businessLicenseNumber) {
        boolean available = !memberReadService.isBusinessLicenseNumberExists(businessLicenseNumber);
        return ResponseEntity.ok(ApiResponse.ok(available, available ? "사용 가능한 사업자 번호입니다" : "이미 등록된 사업자 번호입니다"));
    }



}
