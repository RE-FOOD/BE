package com.iitp.domains.order.repository;

import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.domain.entity.QCart;
import com.iitp.domains.cart.domain.entity.QCartMenu;
import com.iitp.domains.member.domain.entity.QMember;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.domain.entity.QOrder;
import com.iitp.domains.order.dto.response.OrderPaymentMenuList;
import com.iitp.domains.order.dto.response.OrderPaymentResponse;
import com.iitp.domains.payment.domain.QPayment;
import com.iitp.domains.payment.domain.TossPaymentMethod;
import com.iitp.domains.store.domain.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.iitp.domains.order.domain.entity.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom{
    private final JPAQueryFactory queryFactory;


    @Override
    public Optional<Order> findByOrderId(Long orderId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(order)
                        .where(
                                order.isDeleted.eq(false),
                                order.id.eq(orderId)
                        )
                        .fetchOne()
        );
    }


    @Override
    public List<Order> findOrders(String keyword, Long cursorId, Long memberId) {
        QOrder order = QOrder.order;
        QStore store = QStore.store;
        QMember member = QMember.member;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 1. memberId 조건 (필수)
        whereClause.and(order.member.id.eq(memberId));

        // 2. cursorId 조건 (페이징)
        if (cursorId != null) {
            whereClause.and(order.id.gt(cursorId));
        }

        // 3. keyword 조건 (선택적)
        if (keyword != null && !keyword.trim().isEmpty()) {
            whereClause.and(store.name.containsIgnoreCase(keyword.trim()));
        }

        return queryFactory
                .selectFrom(order)
                .join(order.store, store)
                .join(order.member, member)
                .where(whereClause)
                .orderBy(order.id.asc())
                .limit(15) // 한 번에 가져올 최대 개수
                .fetch();
    }


    @Override
    public OrderPaymentResponse findOrderWithDetails(Long orderId, Long memberId) {
        QOrder order = QOrder.order;
        QStore store = QStore.store;
        QMember member = QMember.member;
        QCart cart = QCart.cart;
        QCartMenu cartMenu = QCartMenu.cartMenu;
        QMenu menu = QMenu.menu;
        QPayment payment = QPayment.payment;

        // 1. 주문 기본 정보 조회
        Order orderEntity = queryFactory
                .selectFrom(order)
                .join(order.store, store).fetchJoin()
                .join(order.member, member).fetchJoin()
                .join(order.cart, cart).fetchJoin()
                .where(
                        order.id.eq(orderId),
                        order.member.id.eq(memberId),
                        order.isDeleted.eq(false)
                )
                .fetchOne();

        if (orderEntity == null) {
            return null;
        }

        // 2. CartMenu 데이터 조회
        List<CartMenu> cartMenuEntities = queryFactory
                .selectFrom(cartMenu)
                .where(cartMenu.cartId.eq(orderEntity.getCart().getId()))
                .fetch();

        // 3. 메뉴 정보 조회 및 DTO 변환
        List<OrderPaymentMenuList> menus = cartMenuEntities.stream()
                .map(cartMenuEntity -> {
                    // Menu 정보 조회
                    Menu menuEntity = queryFactory
                            .selectFrom(menu)
                            .where(menu.id.eq(cartMenuEntity.getMenuId()))
                            .fetchOne();

                    return OrderPaymentMenuList.builder()
                            .name(menuEntity != null ? menuEntity.getName() : "알 수 없는 메뉴")
                            .quality(cartMenuEntity.getOrderQuantity())
                            .totalAmount(cartMenuEntity.getDiscountPrice() * cartMenuEntity.getOrderQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        // 4. 결제 정보 조회
        com.iitp.domains.payment.domain.Payment paymentEntity = queryFactory
                .selectFrom(payment)
                .where(payment.orderId.eq(orderId))
                .fetchOne();

        // 5. DTO 생성
        return OrderPaymentResponse.builder()
                .storeName(orderEntity.getStore().getName())
                .orderNumber(orderEntity.getId().toString())
                .requestedAt(Timestamp.valueOf(orderEntity.getCreatedAt()))
                .menus(menus)
                .totalAmount(orderEntity.getTotalAmount())
                .paymentMethod(paymentEntity != null ?
                        paymentEntity.getTossPaymentMethod() : null)
                .memberName(orderEntity.getMember().getNickname())
                .memberNumber(orderEntity.getMember().getPhone())
                .pickupDueTime(orderEntity.getPickupDueTime())
                .build();
    }

}
