package com.iitp.domains.order.service.query;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.dto.CartMenuRedisDto;
import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.dto.response.CartMenuResponse;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.response.*;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.payment.domain.Payment;
import com.iitp.domains.payment.repository.PaymentRepository;
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
import java.util.stream.Collectors;

import com.iitp.global.exception.OrderConflictException;
import com.iitp.global.redis.service.CartRedisService;
import com.iitp.imageUpload.service.query.ImageGetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;
    private final ImageGetService  imageGetService;
    private final PaymentRepository paymentRepository;
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
    public OrderResponse getOrderReady(Long memberId) {
        // TODO :: Service를 통해 호출하는 코드로 리팩토링

        String cacheKey = CART_CACHE_PREFIX + memberId;
        CartRedisDto existingCart = cartRedisService.getCartFromRedis(cacheKey);

        if(existingCart == null) {
            throw new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND);
        }

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


    public OrderPaymentResponse getOrder(Long orderId, Long memberId) {
        // Repository에서 JOIN을 통해 모든 데이터를 한 번에 조회
        OrderPaymentResponse response = orderRepository.findOrderWithDetails(orderId, memberId);

        if (response == null) {
            throw new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND);
        }

        return response;
    }



    public OrderListResponse getOrders(String keyword, Long cursorId, Long memberId) {
        List<Order> orders = orderRepository.findOrders(keyword, cursorId, memberId);

        if(orders.isEmpty()) {
            return null;
        }

        // Order 엔티티를 OrderMenuListResponse로 변환
        List<OrderMenuListResponse> orderResponses = orders.stream()
                .map(order -> {
                    // Store 정보 조회
                    Store store = order.getStore();

                    // Cart에서 첫 번째 메뉴 정보 가져오기 (대표 메뉴)
                    String menuName = "주문 완료"; // 기본값
                    if (order.getCart() != null && !order.getCart().getCartMenus().isEmpty()) {
                        // 첫 번째 메뉴의 이름을 가져옴
                        Long firstMenuId = order.getCart().getCartMenus().get(0).getMenuId();
                        try {
                            Menu firstMenu = menuRepository.findByMenuId(firstMenuId)
                                    .orElse(null);
                            if (firstMenu != null) {
                                menuName = firstMenu.getName();
                            }
                        } catch (Exception e) {
                            // 메뉴 조회 실패 시 기본값 사용
                            log.warn("메뉴 조회 실패: {}", firstMenuId, e);
                        }
                    }

                    // Store 이미지 URL 가져오기 (첫 번째 이미지)
                    String imageUrl = "";
                    if (store.getStoreImages() != null && !store.getStoreImages().isEmpty()) {
                        String imageKey = store.getStoreImages().get(0).getImageKey();
                        try {
                            imageUrl = imageGetService.getGetS3Url(imageKey).preSignedUrl();
                        } catch (Exception e) {
                            log.warn("이미지 URL 조회 실패: {}", imageKey, e);
                        }
                    }

                    return OrderMenuListResponse.builder()
                            .orderId(order.getId())
                            .storeId(store.getId())
                            .storeName(store.getName())
                            .imageUrl(imageUrl)
                            .status(order.getStatus() == OrderStatus.COMPLETED)
                            .menuName(menuName)
                            .build();
                })
                .collect(Collectors.toList());

        // 커서 값 계산
        long prevCursor = orderResponses.get(0).orderId();
        long nextCursor = orderResponses.get(orderResponses.size()-1).orderId();

        return OrderListResponse.builder()
                .prevCursor(prevCursor)
                .nextCursor(nextCursor)
                .orders(orderResponses)
                .build();

    }


    public Store validateStoreExists(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    public Menu validateMenuExists(Long menuId) {
        return menuRepository.findByMenuId(menuId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
    }

    public Order validateOrderExists(Long orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.ORDER_NOT_FOUND));
    }

    public Payment validatePaymentExists(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessage.PAYMENT_NOT_FOUND));
    }

    public String getImageUrl(String imageKey) {
        return imageGetService.getGetS3Url(imageKey).preSignedUrl();
    }

}
