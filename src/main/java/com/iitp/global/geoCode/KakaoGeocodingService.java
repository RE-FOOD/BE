package com.iitp.global.geoCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class KakaoGeocodingService {

    private final WebClient webClient;
    private final String apiKey;

    public KakaoGeocodingService(@Value("${kakao.api.key}") String apiKey) {
        this.apiKey = apiKey;

        // API 키 로그 (보안을 위해 일부만)
        String maskedKey = apiKey.length() > 10 ?
                apiKey.substring(0, 10) + "..." :
                apiKey;
        log.info("Kakao API Key 설정됨: {}", maskedKey);

        this.webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    public GeocodingResult getCoordinates(String address) {
        try {
//            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "/v2/local/search/address.json?query=" + address;

            log.info("=== Kakao API 호출 시작 ===");
            log.info("요청 URL: {}", url);
            log.info("요청 주소: {}", address);
//            log.info("인코딩된 주소: {}", encodedAddress);

            KakaoGeocodingResponse response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.error("=== 4xx 오류 발생 ===");
                        log.error("상태 코드: {}", clientResponse.statusCode());
                        log.error("응답 헤더: {}", clientResponse.headers());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("응답 본문: {}", body);
                                    return Mono.error(new RuntimeException("Kakao API 4xx 오류: " + clientResponse.statusCode()));
                                });
                    })
                    .bodyToMono(KakaoGeocodingResponse.class)
                    .doOnNext(resp -> log.info("=== API 응답 성공 ==="))
                    .block();

            if (response != null && response.documents() != null && !response.documents().isEmpty()) {
                KakaoGeocodingResponse.Document document = response.documents().get(0);

                return new GeocodingResult(
                        Double.parseDouble(document.y()),
                        Double.parseDouble(document.x()),
                        document.address_name(),
                        document.address_type()
                );
            }

            throw new RuntimeException("주소를 찾을 수 없습니다: " + address);

        } catch (WebClientResponseException e) {
            log.error("=== WebClientResponseException 발생 ===");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 헤더: {}", e.getHeaders());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Kakao API 호출 실패: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("=== 일반 Exception 발생 ===");
            log.error("오류 메시지: {}", e.getMessage());
            throw new RuntimeException("주소 변환 중 오류가 발생했습니다.", e);
        }
    }
}