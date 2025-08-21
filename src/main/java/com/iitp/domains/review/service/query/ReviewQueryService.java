package com.iitp.domains.review.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewQueryService {
    //리뷰 조회 : 특정 가게에 대한 조회. 필터링과 페이지네이션

    /**
     * 정렬 기준
     * - 초기 : 최신순
     * - 종류 : 별점 높은 순, 낮은 순
     *
     * 페이지네이션
     * - 1 페이지 = 20개 ?
     * - 무한 스크롤
     *
     *
     * 내용 : 멤버 닉네임, 별점, 생성일자, 내용, 메뉴 목록
     */

    // TODO: 장바구니 구현 후 메뉴 목록 조회 부분 연결



}
