package com.iitp.domains.cart.repository;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.QCart;
import com.iitp.domains.store.domain.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.iitp.domains.cart.domain.entity.QCart.cart;
import static com.iitp.domains.store.domain.entity.QStore.store;

@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepositoryCustom{
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QCart qCart = cart;


    @Override
    public Optional<Cart> findCartData(Long storeId, Long memberId) {

        return Optional.empty();
    }


    @Override
    public Optional<Cart> findByCartId(Long cartId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(cart)
                        .where(
                                cart.isDeleted.eq(false),
                                cart.id.eq(cartId)
                        )
                        .fetchOne()
        );
    }
}
