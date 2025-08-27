package com.iitp.domains.store.domain.entity;

import com.iitp.domains.store.dto.request.MenuUpdateRequest;
import com.iitp.domains.store.dto.request.StoreUpdateRequest;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Menu extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private  String name;

    @Column(name = "info", nullable = false)
    private  String info;

    @Column(name = "price", nullable = false)
    private  int price;

    @Column(name = "daily_discount_percent", nullable = false)
    private int dailyDiscountPercent;

    @Column(name = "daily_quantity", nullable = false)
    private int dailyQuantity;

    @Column(name = "discount_price", nullable = false)
    private int discountPrice;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;



    public void update(MenuUpdateRequest requestDto) {
        if (requestDto.name() != null) this.name = requestDto.name();
        if(requestDto.info() != null) this.info = requestDto.info();
        if(requestDto.dailyQuantity()  > 0) this.dailyQuantity = requestDto.dailyQuantity();
        if(requestDto.imageKey() != null) this.imageKey = requestDto.imageKey();
    }

    public void quantityReduction(int quantity) {
        this.dailyDiscountPercent -= quantity;
    }
}
