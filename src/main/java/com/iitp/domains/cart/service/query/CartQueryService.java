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


public interface CartQueryService {

    String[] getCartDuplicate(Long storeId, Long memberId);


    CartResponse getCartFromRedis(Long memberId);

}
