package com.iitp.domains.cart.repository;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.store.domain.entity.Store;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepositoryCustom {
    Optional<Cart>  findCartData(Long storeId, Long memberId);
    Optional<Cart> findByCartId(Long storeId);
}
