package com.iitp.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        log.warn("인증되지 않은 사용자의 접근 시도 - URI: {}, 오류: {}",
                requestURI, authException.getMessage());

        // JSON 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 응답 본문 생성
        Map<String, Object> errorResponse = createErrorResponse(request, authException);

        // JSON으로 응답 전송
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * 에러 응답 객체 생성
     */
    private Map<String, Object> createErrorResponse(HttpServletRequest request,
                                                    AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("statusCode", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", determineErrorMessage(request, authException));
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return errorResponse;
    }

    /**
     * 상황에 맞는 에러 메시지 결정
     */
    private String determineErrorMessage(HttpServletRequest request,
                                         AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            return "인증 토큰이 필요합니다.";
        } else if (!authHeader.startsWith("Bearer ")) {
            return "올바르지 않은 토큰 형식입니다.";
        } else {
            // JWT 토큰 관련 구체적인 오류
            String errorMessage = authException.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("expired")) {
                    return "토큰이 만료되었습니다.";
                } else if (errorMessage.contains("invalid")) {
                    return "유효하지 않은 토큰입니다.";
                } else if (errorMessage.contains("malformed")) {
                    return "잘못된 형식의 토큰입니다.";
                }
            }
            return "인증에 실패했습니다.";
        }
    }
}