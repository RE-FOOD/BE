package com.iitp.domains.cart.validator;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.repository.CartRepository;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartValidator {
    private final CartRepository cartRepository;

//    private Cart validateCartExists(Long storeId, Long memberId) {
//        return cartRepository.findCartData(storeId, memberId)
//                .orElseThrow( () -> new NotFoundException(ExceptionMessage.DATA_NOT_FOUND));
//    }

    public Cart validateCartExists(Long cartId) {
        return cartRepository.findByCartId(cartId)
                .orElseThrow( () -> new NotFoundException(ExceptionMessage.CART_NOT_FOUND));
    }
}
