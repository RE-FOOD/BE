package com.iitp.domains.member.domain.entity;

import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location")
@Getter
@NoArgsConstructor
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "address", length = 100)
    private String address; // 전체 주소

    @Column(name = "road_address", length = 50)
    private String roadAddress; // 도로명 주소

    @Column(name = "is_most_recent", nullable = false)
    private Boolean isMostRecent = false; //기본 주소 여부(가장 최신에 선택)

    // 위도
    @Column(name = "latitude")
    private Double latitude;

    // 경도
    @Column(name = "longitude")
    private Double longitude;

    @Builder
    public Location(Long memberId, Boolean isMostRecent, String roadAddress, String address,
                    Double latitude, Double longitude) {
        this.memberId = memberId;
        this.isMostRecent = isMostRecent != null ? isMostRecent : false;
        this.address = address;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateAddress(String address, String roadAddress,
                              Double latitude, Double longitude) {
        this.address = address;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setAsMostRecent() {
        this.isMostRecent = true;
    }

    public void unsetAsMostRecent() {
        this.isMostRecent = false;
    }
}
