package com.iitp.global.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.domains.cart.dto.CartMenuRedisDto;
import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CartRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MenuRepository menuRepository;

    private static final String CART_CACHE_PREFIX = "cart:";

    public void deleteCart(String cacheKey) {
        redisTemplate.delete(cacheKey);
    }

    public CartRedisDto addCartItemToCart(CartRedisDto cart, Long menuId, Integer quantity) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));

        CartMenuRedisDto cartMenu = CartMenuRedisDto.fromEntity(menu,quantity);
        CartRedisDto dto = cart.addMenu(cartMenu,cartMenu.discountPrice() * quantity);

        return dto;
    }

    public void saveCartToRedis(String cacheKey, CartRedisDto cart) {
        try {
            String json = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(cacheKey, json);
        } catch (JsonProcessingException e) {
            log.error("장바구니 Redis 저장 실패", e);
            throw new RuntimeException("장바구니 저장 실패", e);
        }
    }

    public CartRedisDto getCartFromRedis(String cacheKey) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                return objectMapper.readValue(json, CartRedisDto.class);
            }
        } catch (JsonProcessingException e) {
            log.error("장바구니 Redis 조회 실패", e);
            redisTemplate.delete(cacheKey);
        }
        return null;
    }

//    // 주문 완료 시 DB에 저장
//    public void saveCartToDatabase(Long memberId) {
//        String cacheKey = CART_CACHE_PREFIX + memberId;
//        Cart cart = getCartFromRedis(cacheKey);
//
//        if (cart != null) {
//            cartRepository.save(cart);
//            redisTemplate.delete(cacheKey); // Redis에서 삭제
//        }
//    }
}
