package com.iitp.domains.cart.domain.entity;

import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CartMenuId.class)
public class CartMenu extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    private Long cartId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;  // 이 필드가 필요함

    public static CartMenu create(Long cartId, Long menuId, Integer quantity) {
        CartMenu item = new CartMenu();
        item.cartId = cartId;
        item.menuId = menuId;
        item.quantity = quantity;
        return item;
    }

    public void setCart(Cart cart) {
        this.cartId = cart.getId();
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}