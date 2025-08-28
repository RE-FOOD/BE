package com.iitp.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


//도메인이 늘어나면 각각 도메인에 같은 클래스들을 만들어 사용예정
@RequiredArgsConstructor
@Getter
public enum ExceptionMessage {

    // 인증 관련
    AUTHENTICATION_FAILED("이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_PRINCIPAL_TYPE("유효하지 않은 인증입니다"),
    AUTHENTICATION_MISSING("인증에 실패했습니다."),

    // 토큰 관련
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰을 찾을 수 없습니다."),

    // 회원 관련
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS("이미 사용 중인 닉네임입니다."),
    PHONE_ALREADY_EXISTS("이미 사용 중인 핸드폰번호입니다."),
    BUSINESSLICENSENUMBER_ALREADY_EXISTS("이미 사용 중인 사업자번호입니다."),
    INVALID_PASSWORD_FORMAT("비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다."),
    INCORRECT_PASSWORD("기존 비밀번호가 틀립니다 다시 입력해주세요"),

    // 주소 관련 에러 메시지 추가
    ADDRESS_GEOCODING_FAILED("주소를 찾을 수 없습니다. 주소를 다시 확인해주세요."),

    // 사업자 승인 관련
    BUSINESS_APPROVAL_PENDING("사업자 등록 승인이 대기 중입니다. 승인 후 로그인해주세요."),

    // 권한 관련
    ACCESS_DENIED("접근 권한이 없습니다."),
    ACCESS_DENIED_NOT_AUTHOR("해당 데이터의 작성자가 아닙니다."),
    INSUFFICIENT_PERMISSION("해당 작업을 수행할 권한이 없습니다."),

    // 카카오 관련
    KAKAO_TOKEN_INVALID("유효하지 않은 카카오 토큰입니다."),
    KAKAO_API_ERROR("카카오 API 호출 중 오류가 발생했습니다."),

    // 일반 오류
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_REQUEST("잘못된 요청입니다."),
    RESOURCE_NOT_FOUND("요청한 리소스를 찾을 수 없습니다."),

    // 게시물 관련
    DATA_NOT_FOUND("데이터 정보가 없음"),
    CART_DATA_DEFERENCE("다른 가게 장바구니 데이터 존재"),
    STORE_NOT_FOUND("가게 데이터 정보가 없음"),
    MENU_NOT_FOUND("메뉴 데이터 정보가 없음"),
    CART_NOT_FOUND("카트 데이터 정보가 없음"),


    // 리뷰 관련
    INVALID_REVIEW_CONTENT_LENGTH("유효하지 않은 리뷰 내용 길이입니다."),
    INVALID_REVIEW_RATING("유효하지 않은 리뷰 별점입니다."),
    ALREADY_EXISTING_REVIEW_OF_ORDER("해당 주문에 대해 이미 리뷰가 존재합니다"),
    REVIEW_NOT_FOUND("리뷰를 찾을 수 없습니다."),


    // 주문 관련
    INVALID_ORDER_STATUS("유효하지 않은 주문 상태 종류입니다."),
    ORDER_NOT_FOUND("주문을 찾을 수 없습니다."),
    ORDER_STATUS_NOT_COMPLETED("주문이 아직 완료되지 않았습니다."),

    // 결과 관련
    PAYMENT_INFO_INVALID("결제 금액 정보가 유효하지 않습니다."),
    PAYMENT_NOT_FOUND("주문을 찾을 수 없습니다."),
    SESSION_EXPIRED("주문 세션 시간이 만료되었습니다 다시 시도해주세요");



    private final String message;
}
