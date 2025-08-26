package com.iitp.domains.member.repository;

import com.iitp.domains.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 이메일로 회원 찾기 (삭제되지 않은 회원만)
    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    // 닉네임으로 회원 찾기 (삭제되지 않은 회원만)
    Optional<Member> findByNicknameAndIsDeletedFalse(String nickname);

    // 전화번호로 회원 찾기 (삭제되지 않은 회원만)
    Optional<Member> findByPhoneAndIsDeletedFalse(String phone);

    // 리프레시 토큰으로 회원 찾기
    Optional<Member> findByRefreshTokenAndIsDeletedFalse(String refreshToken);

    // 닉네임 중복 체크
    boolean existsByNicknameAndIsDeletedFalse(String nickname);

    Optional<Member> findByIdAndIsDeletedFalse(Long id);

    // 리프레시 토큰 삭제 (로그아웃)
    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.refreshToken = null WHERE m.id = :memberId")
    void clearRefreshToken(@Param("memberId") Long memberId);
}
