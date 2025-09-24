package com.iitp.domains.cart.service.command;

import com.iitp.domains.cart.domain.entity.CartMenu;
import com.iitp.domains.cart.dto.CartMenuRedisDto;
import com.iitp.domains.cart.dto.CartRedisDto;
import com.iitp.domains.cart.dto.request.CartCreateRequest;
import com.iitp.domains.cart.dto.request.CartUpdateRequest;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.domains.store.validator.StoreValidator;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import com.iitp.global.redis.service.CartRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface CartCommandService {

    // Redis에 장바구니 저장
    void addCart(Long memberId, CartCreateRequest request);


    void updateCart(Long memberId, CartUpdateRequest request);

    Cart saveCart(Long memberId);


    Cart getCart(Long memberId);

}
