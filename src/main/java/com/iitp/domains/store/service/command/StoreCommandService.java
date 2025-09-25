package com.iitp.domains.store.service.command;

import com.iitp.domains.notification.dto.NotifyParams;
import com.iitp.domains.notification.service.NotificationService;
import com.iitp.domains.order.domain.OrderStatus;
import com.iitp.domains.order.domain.entity.Order;
import com.iitp.domains.order.service.command.OrderCommandService;
import com.iitp.domains.order.service.query.OrderQueryService;
import com.iitp.domains.order.validator.OrderValidator;
import com.iitp.domains.payment.domain.Payment;
import com.iitp.domains.payment.domain.TossPaymentStatus;
import com.iitp.domains.payment.service.PaymentService;
import com.iitp.domains.payment.validator.PaymentValidator;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.domain.entity.StoreImage;
import com.iitp.domains.store.dto.request.StoreCreateRequest;
import com.iitp.domains.store.dto.request.StoreUpdateRequest;
import com.iitp.domains.store.dto.response.InsightResponse;
import com.iitp.domains.store.repository.store.StoreImageRepository;
import com.iitp.domains.store.repository.store.StoreRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.geoCode.GeocodingResult;
import com.iitp.global.geoCode.KakaoGeocodingService;
import com.iitp.global.redis.service.RedisGeoService;
import com.iitp.global.redis.service.StoreRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface StoreCommandService {

    Long createStore(StoreCreateRequest request, Long userId);

    void updateStore(StoreUpdateRequest request,Long storeId, Long userId);

    void deleteStore(Long storeId, Long userId);


    void refusalOrder(Long memberId, Long orderId);


    void confirmOrder(Long memberId, Long orderId);


    InsightResponse findInsight(Long memberId);

}
