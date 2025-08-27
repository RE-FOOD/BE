package com.iitp.domains.order.service.command;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.service.command.CartCommandService;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.member.repository.MemberRepository;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.dto.request.OrderCreateRequest;
import com.iitp.domains.order.repository.OrderRepository;
import com.iitp.domains.payment.service.PaymentService;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.exception.OrderConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final CartCommandService cartCommandService;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;
    private final PaymentService paymentService;

    public String createOrder(OrderCreateRequest request, Long memberId) {
        Cart cart = cartCommandService.saveCart(memberId);

        // TODO 동시성 처리 Redis + Kafka 리팩토링
        for(int i=0; i<cart.getCartMenus().size(); i++) {
            Menu menu = validateMenuExists(memberId);

            if(cart.getCartMenus().get(i).getOrderQuantity() > menu.getDailyQuantity()){
                throw new OrderConflictException(menu.getName());
            }
            menu.quantityReduction(cart.getCartMenus().get(i).getOrderQuantity());
        }

        Store store = validateStoreExists(cart.getStore().getId());
        Member member = validateMemberExists(memberId);

        // 주문 엔티티 생성 (아직 저장하지 않음)
        Order order = request.toEntity(member, store, cart, request.pickupDueAt(), request.reuse());

        // 결제 세션 생성 및 Redis에 임시 저장
        String paymentSessionId = paymentService.createPaymentSession(order, cart);

        return paymentSessionId;
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
}