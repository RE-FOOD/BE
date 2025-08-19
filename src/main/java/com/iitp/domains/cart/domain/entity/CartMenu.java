package com.iitp.domains.cart.domain.entity;

import com.iitp.domains.store.domain.entity.Menu;
import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(CartMenuId.class)
public class CartMenu extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    private Long cartId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "daily_discount_percent", nullable = false)
    private int dailyDiscountPercent;

    @Column(name = "order_quantity", nullable = false)
    private int orderQuantity;

    @Column(name = "discount_price", nullable = false)
    private int discountPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
}