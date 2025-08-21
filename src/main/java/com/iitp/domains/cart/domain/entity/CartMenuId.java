package com.iitp.domains.cart.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartMenuId implements Serializable {

    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "menu_id")
    private Long menuId;
}
