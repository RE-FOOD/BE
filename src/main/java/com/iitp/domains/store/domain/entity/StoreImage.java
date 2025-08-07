package com.iitp.domains.store.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public StoreImage(String imageKey, Store store) {
        this.imageKey = imageKey;
        this.store = store;
    }
}
