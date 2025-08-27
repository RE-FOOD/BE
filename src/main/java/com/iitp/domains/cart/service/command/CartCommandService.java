package com.iitp.domains.cart.service.command;

import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.dto.CartMenuRedisDto;
import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.dto.request.CartCreateRequest;
import com.iitp.domains.cart.dto.request.CartUpdateRequest;
import com.iitp.domains.cart.dto.response.CartMenuResponse;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartCommandService {
    private final CartRepository cartRepository;
    private final StoreRepository storeRepository;
    private final CartRedisService cartRedisService;
    private final MenuRepository menuRepository;
    private final ImageGetService imageGetService;
    private static final String CART_CACHE_PREFIX = "cart:";

    // Redis에 장바구니 저장
    public void addCart(Long memberId, CartCreateRequest request) {
        String cacheKey = CART_CACHE_PREFIX + memberId;
        Store store = validateStoreExists(request.storeId());

        if (request.checkNew()) {
            // 새로운 카트 생성 - 기존 데이터 삭제
            cartRedisService.deleteCart(cacheKey);
            CartRedisDto cartRedisDto = CartRedisDto.fromEntity(store);     // 장바구니 생성
            CartRedisDto dto = cartRedisService.addCartItemToCart(cartRedisDto, request.menuId(), request.quantity()); // 장바구니에 메뉴 추가
            cartRedisService.saveCartToRedis(cacheKey, dto);
        } else {
            // 기존 카트에 아이템 추가
            CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);
            if (existingCart != null) {
                CartRedisDto dto  = cartRedisService.addCartItemToCart(existingCart, request.menuId(), request.quantity());
                cartRedisService.saveCartToRedis(cacheKey, dto);
            } else {
                // 기존 카트가 없으면 새로 생성
                CartRedisDto cartRedisDto = CartRedisDto.fromEntity(store);     // 장바구니 생성

                CartRedisDto dto  = cartRedisService.addCartItemToCart(cartRedisDto, request.menuId(), request.quantity()); // 장바구니에 메뉴 추가
                cartRedisService.saveCartToRedis(cacheKey, dto);
            }
        }
    }


    public void updateCart(Long memberId, CartUpdateRequest request) {
        String cacheKey = CART_CACHE_PREFIX + memberId;
        Store store = validateStoreExists(request.id());

        // 기존 장바구니 데이터 삭제
        cartRedisService.deleteCart(cacheKey);

        // 장바구니 업데이트
        CartRedisDto cartRedisDto = CartRedisDto.fromEntity(store);

        for(CartUpdateRequest.CartMenuListRequest menu : request.menus()){
            // 메뉴가 0개 이상일 경우에만
            if(menu.quantity() > 0){
                cartRedisDto = cartRedisService.addCartItemToCart(cartRedisDto, menu.id(), menu.quantity());
            }
        }
        // 장바구니에 메뉴 추가
        cartRedisService.saveCartToRedis(cacheKey, cartRedisDto);
    }

    public Cart saveCart(Long memberId) {
        String cacheKey = CART_CACHE_PREFIX + memberId;


        CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);


        if(existingCart == null){
            throw new NotFoundException(ExceptionMessage.CART_NOT_FOUND);
        }

        Store store = validateStoreExists(existingCart.id());
        Cart cart = CartRedisDto.toEntity(store, memberId,existingCart );
//        // TODO: 리팩토링
        cartRepository.save(cart);
        cartRepository.flush();

        List<CartMenu> cartMenus = existingCart.menus().stream()
                        .map(menu ->
                            CartMenuRedisDto.toEntity(cart, menu.id(), menu)).toList();

        cart.addMenu(cartMenus);
        cartRepository.save(cart);
        return cart;
    }


    public Cart getCart(Long memberId) {
        // TODO saveCart처럼
        String cacheKey = CART_CACHE_PREFIX + memberId;
        CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);

        Store store = validateStoreExists(existingCart.id());
        Cart cart = CartRedisDto.toEntity(store, memberId, existingCart );

        List<CartMenu> cartMenus = existingCart.menus().stream()
                .map(menu ->
                        CartMenuRedisDto.toEntity(cart, menu.id(), menu)).toList();


        return cart.addMenu(cartMenus);
    }

    private Store validateStoreExists(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    private Menu validateMenuExists(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }


}
