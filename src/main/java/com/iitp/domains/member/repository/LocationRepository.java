package com.iitp.domains.member.repository;

import com.iitp.domains.member.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface LocationRepository extends JpaRepository<Location, Long>, LocationRepositoryCustom {

    // 회원의 가장 최근 위치 조회
    Optional<Location> findByMemberIdAndIsMostRecentTrueAndIsDeletedFalse(Long memberId);

    // 회원의 모든 주소 조회 (기본 주소 우선, 생성일 역순)
    List<Location> findAllByMemberIdAndIsDeletedFalseOrderByIsMostRecentDescCreatedAtDesc(Long memberId);

    // 회원의 모든 위치를 최근이 아닌 것으로 변경
    @Modifying
    @Transactional
    @Query("UPDATE Location l SET l.isMostRecent = false WHERE l.memberId = :memberId AND l.isDeleted = false")
    void updateAllToNotMostRecent(@Param("memberId") Long memberId);

    // 회원의 주소 개수 조회
    long countByMemberIdAndIsDeletedFalse(Long memberId);

    // ID와 회원ID로 주소 조회
    Optional<Location> findByIdAndMemberIdAndIsDeletedFalse(Long id, Long memberId);
}