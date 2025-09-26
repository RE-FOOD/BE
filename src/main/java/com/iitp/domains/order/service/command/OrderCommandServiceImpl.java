package com.iitp.domains.order.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.validator.MemberValidator;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.OrderEventDto;
import com.iitp.domains.order.dto.request.OrderCreateRequest;
import com.iitp.domains.order.event.OrderProducer;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.payment.service.PaymentService;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.MenuRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderCommandServiceImpl implements OrderCommandService {
    private final OrderRepository orderRepository;
    private final CartCommandService cartCommandService;
    private final PaymentService paymentService;
    private final MenuRedisService menuRedisService;
    private final StoreValidator storeValidator;
    private final MenuValidator menuValidator;
    private final MemberValidator memberValidator;

    private final OrderProducer orderProducer;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String ORDER_MENU_QUANTITY_KEY = "order:menu:quantity";

    public String createOrder(OrderCreateRequest request, Long memberId) {
        Cart cart = cartCommandService.getCart(memberId);

        // TODO 동시성 처리 Redis + Kafka 리팩토링
        for(int i=0; i<cart.getCartMenus().size(); i++) {
            Menu menu = menuValidator.validateMenuExists(memberId);

            // Redis 동시성 제어 & 수량 보장
            // 재고 수량 반환
            long stockQuantity = menuRedisService.orderMenu(cart.getStore().getId(), menu.getId(), cart.getCartMenus().get(i).getOrderQuantity());

            // Kafka로 주문 요청 전송
            // memberId, menuId, 주문 후 재고수량 이벤트 저장
            orderProducer.sendOrderIssueRequest(
                    OrderEventDto.IssueMessage.builder()
                            .memberId(memberId)
                            .menuId(menu.getId())
                            .stockQuantity(stockQuantity)
                            .build()
            );

//            // 재고 수량 부족
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                //            if(cart.getCartMenus().get(i).getOrderQuantity() > menu.getDailyQuanti                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ty()){
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                //                throw new OrderConflictException(menu.getName());
//            }
//
//            // 재고 감소
//            menu.quantityReduction(cart.getCartMenus().get(i).getOrderQuantity());
        }

        Store store = storeValidator.validateStoreExists(cart.getStore().getId());
        Member member = memberValidator.validateMemberExists(memberId);

        // 주문 엔티티 생성 (아직 저장하지 않음)
        Order order = request.toEntity(member, store, cart, request.pickupDueAt(), request.reuse());

        // 결제 세션 생성 및 Redis에 임시 저장
        String paymentSessionId = paymentService.createPaymentSession(order, cart);

        return paymentSessionId;
    }

    public Order orderSave(Order order) {
        return orderRepository.save(order);
    }

    // 이벤트 처리 메서드
    public String issueOrder(OrderEventDto.IssueMessage message){

    }
}