package com.iitp.domains.member.config;

import com.iitp.domains.member.dto.KakaoUserInfoDto;
import com.iitp.global.exception.BadRequestException;
import com.iitp.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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

            KakaoUserInfoDto userInfo = response.getBody();
            if (userInfo == null) {
                log.error("카카오 사용자 정보 조회 실패: 응답이 null");
                throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
            }

            log.info("카카오 사용자 정보 조회 성공: email={}, nickname={}",
                    userInfo.getEmail(), userInfo.getNickname());
            return userInfo;

        } catch (HttpClientErrorException e) {
            log.error("카카오 API 호출 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().is4xxClientError()) {
                throw new BadRequestException(ExceptionMessage.KAKAO_TOKEN_INVALID);
            } else {
                throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
            }
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 예외 발생", e);
            throw new BadRequestException(ExceptionMessage.KAKAO_API_ERROR);
        }
    }
}
