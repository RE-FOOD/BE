package com.iitp.domains.cart.repository;

import com.iitp.domains.cart.domain.entity.Cart;
import com.iitp.domains.cart.domain.entity.QCart;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepositoryCustom{
    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private static final QCart qCart = QCart.cart;


    @Override
    public Optional<Cart> findCartData(Long storeId, Long memberId) {

        return Optional.empty();
    }
}
