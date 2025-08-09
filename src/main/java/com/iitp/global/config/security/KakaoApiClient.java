package com.iitp.global.config.security;

import com.iitp.domains.member.dto.KakaoUserInfoDto;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


@Component
@Slf4j
public class KakaoApiClient {

    @Value("${kakao.api.user-info-url}")
    private String kakaoUserInfoUrl;

    public KakaoUserInfoDto getUserInfo(String accessToken) {
        log.debug("카카오 사용자 정보 조회 시작");

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<KakaoUserInfoDto> response = restTemplate.exchange(
                    kakaoUserInfoUrl,
                    HttpMethod.GET,
                    entity,
                    KakaoUserInfoDto.class
            );

            // 응답 본문 확인
            KakaoUserInfoDto userInfo = response.getBody();
            if (userInfo == null) {
                log.error("카카오 사용자 정보 조회 실패: 응답 본문이 null - 상태코드: {}", response.getStatusCode());
                throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
            }

            // 필수 필드 검증
            if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
                log.error("카카오 사용자 정보에 이메일이 없음: {}", userInfo);
                throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
            }

            log.info("카카오 사용자 정보 조회 성공: email={}, nickname={}",
                    userInfo.getEmail(), userInfo.getNickname());
            return userInfo;

        } catch (HttpClientErrorException e) {
            log.error("카카오 API 클라이언트 오류: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            // 401 Unauthorized - 토큰 문제
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BadRequestException(ExceptionMessage.KAKAO_TOKEN_INVALID);
            }
            // 기타 4xx 오류
            else if (e.getStatusCode().is4xxClientError()) {
                throw new BadRequestException(ExceptionMessage.KAKAO_TOKEN_INVALID);
            }
            // 예상치 못한 오류
            else {
                throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
            }

        } catch (HttpServerErrorException e) {
            log.error("카카오 API 서버 오류: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);

        } catch (BadRequestException e) {
            // 이미 처리된 비즈니스 예외는 다시 던지기
            throw e;

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 예상치 못한 예외 발생", e);
            throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
        }
    }

    /**
     * 액세스 토큰 유효성 사전 검증
     */
    private void validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.error("카카오 액세스 토큰이 null 또는 빈 문자열");
            throw new BadRequestException(ExceptionMessage.KAKAO_TOKEN_INVALID);
        }
    }
}
