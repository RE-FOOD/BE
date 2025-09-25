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
import com.iitp.domains.order.validator.OrderValidator;
import com.iitp.domains.payment.domain.Payment;
import com.iitp.domains.payment.repository.PaymentRepository;
import com.iitp.domains.payment.validator.PaymentValidator;
import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.dto.response.InsightResponse;
import com.iitp.domains.store.dto.response.StoreOrderListResponse;
import com.iitp.domains.store.repository.menu.MenuRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.domains.store.validator.MenuValidator;
import com.iitp.domains.store.validator.StoreValidator;
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


public interface OrderQueryService {
    Order findExistingOrder(Long orderId);


    List<Menu> findMenuList(Long orderId);

    OrderResponse getOrderReady(Long memberId);


    OrderPaymentResponse getOrder(Long orderId, Long memberId);


    OrderListResponse getOrders(String keyword, Long cursorId, Long memberId);

    InsightResponse findInsight(Long storeId);


    String getImageUrl(String imageKey);

    List<StoreOrderListResponse> findOrdersWithMenuInfo(Long storeId, Long cursorId);
}
