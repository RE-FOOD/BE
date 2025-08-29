package com.iitp.domains.store.domain.entity;

import com.iitp.domains.review.domain.entity.Review;
import com.iitp.domains.member.domain.entity.Member;
import com.iitp.domains.favorite.domain.entity.Favorite;
import com.iitp.domains.store.domain.Category;
import com.iitp.domains.store.domain.StoreStatus;
import com.iitp.domains.store.dto.request.StoreUpdateRequest;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Store extends BaseEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StoreStatus status;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Builder.Default
    @Column(name = "max_percent", nullable = false)
    private Integer maxPercent = 0;


    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> storeImages = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();


    public void update(StoreUpdateRequest requestDto) {
        if (requestDto.name() != null) this.name = requestDto.name();
        if(requestDto.phoneNumber() != null) this.phoneNumber = requestDto.phoneNumber();
        if(requestDto.address() != null) this.address = requestDto.address();
        if(requestDto.description() != null) this.description = requestDto.description();
        if(requestDto.origin() != null) this.origin = requestDto.origin();
        if(requestDto.openTime() != null) this.openTime = LocalTime.from(requestDto.openTime().toLocalDateTime());
        if(requestDto.closeTime() != null) this.closeTime = LocalTime.from(requestDto.closeTime().toLocalDateTime());
    }

    public void updatePercent(int maxPercent){
        this.maxPercent = maxPercent;
    }

    public void updateStatus(){
        this.status = StoreStatus.OPEN;
    }

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
    }

    public void removeFavorite(Favorite favorite) {
        favorites.remove(favorite);
    }

}
