package com.iitp.domains.member.controller.command;


import com.iitp.domains.member.domain.entity.Location;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.dto.responseDto.LocationResponseDto;
import com.iitp.domains.auth.dto.responseDto.MemberLogInResponseDto;
import com.iitp.domains.auth.dto.responseDto.MemberSignupResponseDto;
import com.iitp.domains.auth.dto.responseDto.StoreSignupResponseDto;
import com.iitp.domains.member.service.command.EmailCreateService;
import com.iitp.domains.member.service.command.MemberCommandService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members/business")
@Slf4j
@Tag(name="사업자번호 승인 관리", description = "사업자 등록 승인 관련 API")
public class BusinessCommandController {
    private final EmailCreateService emailCreateService;
    private final MemberCommandService memberCommandService;

    @GetMapping("/approve/{memberId}")
    public ResponseEntity<String> approveBusinessRegistration(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId) {

        log.info("사업자 승인 요청 - memberId: {}", memberId);

        try {
            // 이미 승인된 경우
            if (memberCommandService.isBusinessApproved(memberId)) {
                log.info("이미 승인된 사업자 - memberId: {}", memberId);
                return ResponseEntity.ok()
                        .header("Content-Type", "text/html; charset=UTF-8")
                        .body(emailCreateService.createAlreadyApprovedPage());
            }

            // 승인 처리
            memberCommandService.approveBusinessMember(memberId);

            log.info("사업자 승인 성공 - memberId: {}", memberId);
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailCreateService.createSuccessPage());

        } catch (Exception e) {
            log.error("사업자 승인 실패 - memberId: {}, error: {}", memberId, e.getMessage());
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(emailCreateService.createErrorPage(e.getMessage()));
        }
    }
}
