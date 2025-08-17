package com.iitp.domains.member.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iitp.domains.member.domain.BusinessApprovalStatus;
import com.iitp.domains.favorite.domain.entity.Favorite;
import com.iitp.domains.member.domain.EnvironmentLevel;
import com.iitp.domains.member.domain.JoinType;
import com.iitp.domains.member.domain.Role;
import com.iitp.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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

    @Column(nullable = true, length = 30, unique = true)
    private String nickname;

    @Column(length = 20, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "pay_type", length = 8)
    private String payType;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_type", nullable = false)
    private JoinType joinType;

    @Column(name = "environment_point", nullable = false)
    private Integer environmentPoint = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment_level", nullable = true)
    private EnvironmentLevel environmentLevel = EnvironmentLevel.SPROUT;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "dish_count", nullable = false)
    private Integer dishCount = 0;

    @Column(name = "business_license_number", length = 20, unique = true)
    private String businessLicenseNumber; // 사업자 번호

    @Enumerated(EnumType.STRING)
    @Column(name = "is_business_approved")
    private BusinessApprovalStatus isBusinessApproved; // 사장님 응답있을 시

    @JsonIgnore // 응답에서 제외
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @JsonIgnore // 응답에서 제외
    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @OneToMany(mappedBy = "memberId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    @Builder
    public Member(String email, String nickname, String phone,
                  Role role, JoinType joinType, EnvironmentLevel environmentLevel,
                  String businessLicenseNumber) {
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
        this.role = role != null ? role : Role.ROLE_USER;
        this.joinType = joinType;
        this.environmentLevel = environmentLevel != null ? environmentLevel : EnvironmentLevel.SPROUT;
        this.businessLicenseNumber = businessLicenseNumber;
        this.environmentPoint = 0;
        this.orderCount = 0;
        this.dishCount = 0;
        if (role == Role.ROLE_STORE) {
            this.isBusinessApproved = BusinessApprovalStatus.PENDING;
        }
    }

    // 일반 사용자 생성
    public static Member createMember(String email, String nickname, String phone) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .phone(phone)
                .role(Role.ROLE_USER)
                .joinType(JoinType.KAKAO)
                .environmentLevel(EnvironmentLevel.SPROUT)
                .build();
    }

    // 사업자 생성
    public static Member createStore(String email, String phone, String businessLicenseNumber) {
        return Member.builder()
                .email(email)
                .phone(phone)
                .role(Role.ROLE_STORE)
                .joinType(JoinType.KAKAO)
                .businessLicenseNumber(businessLicenseNumber)
                .build();
    }

    /**
     * 비즈니스 로직 메서드
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    // refreshToken 제거
    public void removeRefreshToken() {
        this.refreshToken = null;
    }

    // fcmToken 제거
    public void removeFcmToken() {
        this.fcmToken = null;
    }

    // 사업자 등록 승인 처리 (승인)
    public static void approveBusinessStatus(Member member) {
        member.isBusinessApproved = BusinessApprovalStatus.APPROVED;
    }
    // 사업자 등록 승인 처리 (대기)
    public static void pendingBusinessStatus(Member member) {
        member.isBusinessApproved = BusinessApprovalStatus.PENDING;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * 연관관계 편의 메서드
     */

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
    }

    public void removeFavorite(Favorite favorite) {
        favorites.remove(favorite);
    }

}
