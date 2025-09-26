package com.iitp.global.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MenuRedisService {
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String ORDER_MENU_QUANTITY_KEY = "order:menu:quantity";
    private static final String COUPON_LOCK_KEY = "coupon:lock";
    private static final long LOCK_WAIT_TIME = 10;
    private static final long LOCK_LEASE_TIME = 30;

    // 가게의 메뉴에 대한 데이터 추가
    public void createMenu (Long memberId, Long menuId, int quantity) {
        String quantityKey = ORDER_MENU_QUANTITY_KEY  + memberId + "/" + menuId;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(quantityKey);
        atomicLong.set(quantity);
    }

    // 가게의 메뉴에 대한 데이터 삭제
    public void deleteMenu (Long memberId, Long menuId) {
        String quantityKey = ORDER_MENU_QUANTITY_KEY  + memberId + "/" + menuId;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(quantityKey);
        atomicLong.delete();
    }

    // 메뉴에 대한 수량 변경
    public long orderMenu(Long memberId, Long menuId, int quantity) {
        String quantityKey = ORDER_MENU_QUANTITY_KEY + memberId + "/" + menuId;
        String lockKey = "lock:menu_quantity:" + memberId + ":" + menuId;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Lock 획득 시도 (10초 대기, 30초 유지)
            if (lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS)) {
                try {
                    RAtomicLong atomicLong = redissonClient.getAtomicLong(quantityKey);

                    long currentQuantity = atomicLong.get();

                    // 재고 부족 확인
                    if (currentQuantity < quantity) {
                        throw new RuntimeException("재고 부족. 현재: " + currentQuantity + ", 요청: " + quantity);
                    }

                    // 수량 차감
                    long newQuantity = atomicLong.addAndGet(-quantity);

                    log.info("메뉴 수량 차감 완료. memberId: {}, menuId: {}, 차감: {}, 남은 수량: {}",
                            memberId, menuId, quantity, newQuantity);

                    return newQuantity;

                } finally {
                    // Lock 해제
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new RuntimeException("Lock 획득 실패");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock 획득 중 인터럽트 발생", e);
        } catch (Exception e) {
            log.error("메뉴 수량 변경 실패. memberId: {}, menuId: {}, quantity: {}",
                    memberId, menuId, quantity, e);
            throw new RuntimeException("메뉴 수량 변경 실패", e);
        }
    }
}
