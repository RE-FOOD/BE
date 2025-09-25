package com.iitp.domains.notification.controller.query;

import com.iitp.domains.notification.dto.NotificationResponse;
import com.iitp.domains.notification.service.NotificationService;
import com.iitp.domains.store.dto.response.StoreListTotalResponse;
import com.iitp.global.common.response.ApiResponse;
import com.iitp.global.common.response.TwoWayCursorListResponse;
import com.iitp.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림 API", description = "알림 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationQueryController {
    private final NotificationService notificationService;

    @Operation(summary = "나의 알림 목록 조회", description = "기본 15개씩 조회 및 전체 알림 읽음 처리")
    @GetMapping("/me")
    public ApiResponse<TwoWayCursorListResponse<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "cursorId", defaultValue = "0") Long cursorId,
            @RequestParam(value = "limit", defaultValue = "15") int limit
    ) {
        return ApiResponse.ok(notificationService.findMyNotifications(userDetails.getMemberId(), cursorId, limit));
    }

}
