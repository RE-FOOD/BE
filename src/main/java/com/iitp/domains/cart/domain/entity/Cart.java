package com.iitp.domains.cart.domain.entity;

import com.iitp.domains.store.domain.entity.Store;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Cart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // TODO :: user연동 되면 연관매핑
    private Long memberId;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartMenu> cartMenus = new ArrayList<>();


//    public static Cart create(Long memberId) {
//        Cart cart = new Cart();
//        cart.memberId = memberId;
//        return cart;
//    }
//
//    public void addItem(CartMenu item) {
//        cartItems.add(item);
//        item.setCart(this);
//    }
//
//    public void clearItems() {
//        cartItems.clear();
//        totalPrice = 0;
//    }
}

