package com.iitp.domains.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @JsonIgnore // 응답에서 제외
    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "pay_type", length = 8)
    private String payType;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_type", nullable = false)
    private JoinType joinType;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment_level", nullable = false)
    private EnvironmentLevel environmentLevel = EnvironmentLevel.SPROUT;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "dish_count", nullable = false)
    private Integer dishCount = 0;

    @Column(name = "business_license_number", length = 20)
    private String businessLicenseNumber; // 사업자 번호

    @Column(name = "is_business_approved")
    private Boolean isBusinessApproved; // 사장님 응답있을 시

    @JsonIgnore // 응답에서 제외
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @JsonIgnore // 응답에서 제외
    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @OneToMany(mappedBy = "memberId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

    @Builder
    public Member(String email, String password, String nickname, String phone,
                  Role role, JoinType joinType, EnvironmentLevel environmentLevel) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
        this.role = role != null ? role : Role.ROLE_USER;
        this.joinType = joinType;
        this.environmentLevel = environmentLevel != null ? environmentLevel : EnvironmentLevel.SPROUT;
        this.orderCount = 0;
        this.dishCount = 0;
    }

    // 비즈니스 로직 메서드들
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
