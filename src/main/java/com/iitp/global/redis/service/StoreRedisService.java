package com.iitp.global.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.domains.store.dto.response.StoreDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String STORE_CACHE_KEY = "store_detail";
    private static final long CACHE_TTL = 3600;

    public void cacheStoreDetail(StoreDetailResponse storeDetail) {
        try {
            String json = objectMapper.writeValueAsString(storeDetail);
            redisTemplate.opsForValue().set(STORE_CACHE_KEY, json, Duration.ofSeconds(CACHE_TTL));
        } catch (JsonProcessingException e) {
            log.error("캐시 저장 중 오류 발생", e);
            // 캐시 저장 실패 시 예외를 던지거나 무시
        }
    }

    public StoreDetailResponse getCachedStoreDetail() {
        try {
            String json = redisTemplate.opsForValue().get(STORE_CACHE_KEY);
            if (json != null) {
                return objectMapper.readValue(json, StoreDetailResponse.class);
            }
        } catch (JsonProcessingException e) {
            log.error("캐시 조회 중 오류 발생", e);
            // 캐시 조회 실패 시 캐시 삭제
            clearCache();
        }
        return null;
    }

    public void clearCache() {
        redisTemplate.delete(STORE_CACHE_KEY);
    }

    public boolean hasCachedData() {
        return redisTemplate.hasKey(STORE_CACHE_KEY);
    }
}