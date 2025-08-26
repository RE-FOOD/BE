package com.iitp.domains.order.service.query;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.dto.CartMenuRedisDto;
import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.dto.response.CartMenuResponse;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.response.OrderMenuResponse;
import com.iitp.domains.order.dto.response.OrderResponse;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;

import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.iitp.global.exception.OrderConflictException;
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;
    private final CartCommandService cartCommandService;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;
    private final ImageGetService  imageGetService;
    private static final String CART_CACHE_PREFIX = "cart:";
    private final CartRedisService cartRedisService;

    public Order findExistingOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND));
        if (order.getIsDeleted()) {
            throw new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND);
        }
        return order;
    }


    public List<Menu> findMenuList(Long orderId) {
        // Order -> Cart -> CartMenu -> Menu 리스트 조회
        // 1. 객체적으로 접근하기. 근데 지금은 CartMenu에서 Menu 엔티티 객체가 아닌 ID만을 FK로 갖고 있음.
        // 2. 쿼리를 통해 접근하기. QueryDSL을 써야 할듯? SELECT * FROM MENU WHERE

        return null;
    }

    @Transactional
    public OrderResponse getOrder(Long memberId) {

        String cacheKey = CART_CACHE_PREFIX + memberId;
        CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);

        Store store = validateStoreExists(existingCart.id());
        List<OrderMenuResponse> menus = new ArrayList<>();

        for(CartMenuRedisDto cartMenu : existingCart.menus()) {
            Menu menu = validateMenuExists(cartMenu.id());

            menus.add(new OrderMenuResponse(
                    menu.getName(),
                    cartMenu.orderQuantity(),
                    getImageUrl(menu.getImageKey()),
                    cartMenu.discountPrice()
            ));
        }

        // TODO 동시성 처리 Redis + Kafka 리팩토링
        for(int i=0; i<existingCart.menus().size(); i++) {
            Menu menu = validateMenuExists(memberId);

            if(existingCart.menus().get(i).orderQuantity() > menu.getDailyQuantity()){
                throw new OrderConflictException(menu.getName());
            }
        }

        OrderResponse response = OrderResponse.builder()
                .name(store.getName())
                .address(store.getAddress())
                .totalCoast(existingCart.totalCoast())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .menus(menus)
                .build();

        return response;
    }



    private Store validateStoreExists(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    private Menu validateMenuExists(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }


    private Member validateMemberExists(Long memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.MEMBER_NOT_FOUND));
    }

    private String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }
}
