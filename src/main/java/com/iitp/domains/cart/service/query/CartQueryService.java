package com.iitp.domains.cart.service.query;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.dto.response.CartMenuResponse;
import com.iitp.domains.cart.dto.response.CartResponse;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.global.exception.ConflictException;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartQueryService {
    private final CartRepository cartRepository;
    private final CartRedisService cartRedisService;
    private final ImageGetService imageGetService;

    private static final String CART_CACHE_PREFIX = "cart:";



    public String getCartDuplicate(Long storeId, Long memberId) {
        String cacheKey = CART_CACHE_PREFIX + memberId;

        // 기존 카트에 저장된 데이터 호출
        CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);

        if(existingCart == null) {
            return "장바구니 데이터 없음";
        }else if(existingCart.id().equals(storeId)) {
            return "동일 가게 데이터 존재";
        }else{
            throw new ConflictException(ExceptionMessage.CART_DATA_DEFERENCE);
        }
    }



    public CartResponse getCartFromRedis(Long memberId) {
        String cacheKey = CART_CACHE_PREFIX + memberId;

        // 기존 카트에 저장된 데이터 호출
        CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);

        String storeImageUrl = imageGetService.getGetS3Url(existingCart.imageKey()).preSignedUrl();

        List<CartMenuResponse> menuResponse = existingCart.menus().stream()
                .map(result ->
                        CartMenuResponse.insertImgURL(result, imageGetService.getGetS3Url(result.imageKey()).preSignedUrl()))
                .toList();

        CartResponse response = new CartResponse(storeImageUrl, existingCart.name(), existingCart.totalCoast(), menuResponse);

        return response;
    }





    private Cart validateCartExists(Long storeId, Long memberId) {
        return cartRepository.findCartData(storeId, memberId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

}
