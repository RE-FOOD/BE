package com.iitp.domains.order.repository;

import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.domain.entity.QCart;
import com.iitp.domains.cart.domain.entity.QCartMenu;
import com.iitp.domains.member.domain.entity.QMember;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.domain.entity.QOrder;
import com.iitp.domains.order.dto.response.OrderPaymentMenuList;
import com.iitp.domains.order.dto.response.OrderPaymentResponse;
import com.iitp.domains.payment.domain.QPayment;
import com.iitp.domains.payment.domain.TossPaymentMethod;
import com.iitp.domains.store.domain.entity.*;
import com.iitp.domains.store.dto.response.InsightResponse;
import com.iitp.domains.store.dto.response.MonthlyRevenueDto;
import com.iitp.domains.store.dto.response.StoreOrderListResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    // 가게 아이디를 통한 오더 찾기
    @Override
    public List<Order> findByStoreId(Long storeId, Long cursorId) {
        QStore store = QStore.store;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 1. memberId 조건 (필수)
        whereClause.and(order.store.id.eq(storeId));

        // 2. cursorId 조건 (페이징)
        if (cursorId != null) {
            whereClause.and(order.id.gt(cursorId));
        }

        return queryFactory
                .selectFrom(order)
                .join(order.store, store)
                .where(whereClause)
                .orderBy(order.id.asc())
                .limit(15) // 한 번에 가져올 최대 개수
                .fetch();
    }

    @Override
    public List<StoreOrderListResponse> findOrdersWithMenuInfo(Long storeId, Long cursorId) {
        QOrder order = QOrder.order;
        QCart cart = QCart.cart;
        QCartMenu cartMenu = QCartMenu.cartMenu;
        QMenu menu = QMenu.menu;

        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(order.store.id.eq(storeId));

        if (cursorId != null) {
            whereClause.and(order.id.gt(cursorId));
        }

        // Order와 Cart를 JOIN하여 주문 정보 조회
        List<Order> orders = queryFactory
                .selectFrom(order)
                .join(order.cart, cart).fetchJoin()
                .where(whereClause)
                .orderBy(order.id.asc())
                .limit(15)
                .fetch();

        // 각 주문별로 메뉴 정보 조회 (Store도 함께 fetchJoin)
        return orders.stream()
                .map(orderEntity -> {
                    // CartMenu를 통해 Menu 정보 조회 (Store도 함께)
                    List<String> menus = queryFactory
                            .select(menu.name)
                            .from(menu)
                            .join(cartMenu).on(cartMenu.menuId.eq(menu.id))
                            .join(menu.store)
                            .where(cartMenu.cartId.eq(orderEntity.getCart().getId()))
                            .fetch();

                    return StoreOrderListResponse.builder()
                            .orderId(orderEntity.getId())
                            .pickupDueTime(orderEntity.getPickupDueTime())
                            .menus(menus)
                            .menuCount(menus.size()-1)
                            .totalAmount(orderEntity.getTotalAmount())
                            .status(orderEntity.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public InsightResponse findInsight(Long storeId) {
        QOrder order = QOrder.order;
        QCart cart = QCart.cart;
        QCartMenu cartMenu = QCartMenu.cartMenu;
        QMenu menu = QMenu.menu;

        // 1. 오늘 총 매출 금액 조회
        Integer todayTotalRevenue = queryFactory
                .select(order.totalAmount.sum())
                .from(order)
                .where(
                        order.status.eq(OrderStatus.COMPLETED),  // 완료된 주문만
                        order.store.id.eq(storeId),
                        order.createdAt.goe(LocalDateTime.now().toLocalDate().atStartOfDay()),
                        order.createdAt.lt(LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay())
                )
                .fetchOne();

        // 2. 30일 기준 가게 주문 인기 메뉴 탑 3의 메뉴 이름
        List<String> popularMenus = queryFactory
                .select(menu.name)
                .from(cartMenu)
                .join(menu).on(cartMenu.menuId.eq(menu.id))
                .join(cart).on(cartMenu.cartId.eq(cart.id))
                .join(order).on(cart.id.eq(order.cart.id))
                .where(
                        order.store.id.eq(storeId),
                        order.createdAt.goe(LocalDateTime.now().minusDays(30)),
                        order.status.eq(OrderStatus.COMPLETED)  // 완료된 주문만
                )
                .groupBy(menu.id, menu.name)
                .orderBy(cartMenu.count().desc())
                .limit(3)
                .fetch();

        // 3. 최근 4개월의 월 매출금액
        List<MonthlyRevenueDto> monthlyRevenues = queryFactory
                .select(Projections.constructor(MonthlyRevenueDto.class,
                        Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m')", order.createdAt),
                        order.totalAmount.sum()
                ))
                .from(order)
                .where(
                        order.store.id.eq(storeId),
                        order.createdAt.goe(LocalDateTime.now().minusMonths(4)),
                        order.status.eq(OrderStatus.COMPLETED)
                )
                .groupBy(Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m')", order.createdAt))
                .orderBy(Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m')", order.createdAt).asc())
                .fetch();

        // Map으로 변환
        Map<String, Integer> monthAmountMap = monthlyRevenues.stream()
                .collect(Collectors.toMap(
                        MonthlyRevenueDto::yearMonth,
                        revenue -> revenue.totalRevenue().intValue()
                ));

        return InsightResponse.builder()
                .salesAmount(todayTotalRevenue != null ? todayTotalRevenue : 0)
                .popularMenu(popularMenus)
                .monthAmount(monthAmountMap)
                .build();
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
