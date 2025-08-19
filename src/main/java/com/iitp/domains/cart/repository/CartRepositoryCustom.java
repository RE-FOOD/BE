package com.iitp.domains.cart.repository;

import com.iitp.domains.cart.domain.entity.Cart;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepositoryCustom {
    Optional<Cart>  findCartData(Long storeId, Long memberId);
}
