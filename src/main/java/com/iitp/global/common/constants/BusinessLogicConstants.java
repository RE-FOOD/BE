package com.iitp.global.common.constants;

public class BusinessLogicConstants {
    /**
     * Page Limit
     */
    public static final int PAGING_LIMIT = 10;
    public static final int STORE_SECTION_LIST_PAGING_LIMIT = 6;

    /**
     * AWS
     */
    public static final int MAX_S3_IMAGE_KEY_LENGTH = 50;

    /**
     * Environment
     * 친환경 관련 상수
     *
     * 새로운 환경 포인트 로직:
     * - 주문금액 250원당 1포인트 (최대 200포인트)
     * - 다회용기 사용시 50포인트
     * - 레벨업 요구 포인트: L2=800p, L3=1600p, L4=3200p
     */

    // 레벨업 요구 포인트 (새로운 기준)
    public static final int ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT = 800;    // L2 요구 포인트
    public static final int ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT = 2400;   // L3 요구 포인트
    public static final int ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT = 5600; // L4 요구 포인트
    public static final int ENVIRONMENT_LEVEL_FOUR_REQUIRED_POINT = 5600;  // 최고 레벨 (더 이상 올라갈 레벨 없음)

    // 환경 포인트 부여 기준
    public static final int ENVIRONMENT_MAX_POINT_PER_ORDER = 200;         // 주문당 최대 포인트
    public static final int ENVIRONMENT_POINT_PER_REUSING_CONTAINER = 50;  // 다회용기 사용시 포인트

    // 포인트 계산용 상수
    public static final int PRICE_UNIT_FOR_POINT = 250;                    // 250원당 1포인트

    // 환경 기여도 계산 (탄소 절약량)
    public static final double SAVED_CARBON_KG_PER_ORDERED_MENU = 0.3;           // 픽업 주문당 절약되는 CO2 (kg)
    public static final double SAVED_CARBON_KG_PER_REUSING_CONTAINER = 0.18;     // 다회용기당 절약되는 CO2 (kg)

    // 환경 기여도 계산 (나무 절약량)
    public static final double SAVED_TREE_PER_ORDERED_MENU = 0.045;              // 픽업 주문당 절약되는 나무 (그루)
    public static final double SAVED_TREE_PER_REUSING_CONTAINER = 0.027;         // 다회용기당 절약되는 나무 (그루)
    /**
     * Auth
     */
    public static final int MAX_REFRESH_TOKEN_LENGTH = 500;
    public static final int MAX_FCM_TOKEN_LENGTH = 255;


    /**
     * Member
     */
    public static final int MAX_EMAIL_LENGTH = 320; // 이메일 로컬 64자, @, 이메일 도메인 255자
    public static final int MAX_PASSWORD_LENGTH = 255;
    public static final int MAX_NICKNAME_LENGTH = 30;
    public static final int MAX_PHONE_LENGTH = 30;
    public static final int MAX_BUSINESS_LICENSE_NUMBER_LENGTH = 20;    // 사업자 등록번호 길이는 숫자만 포함했을 때 10. `-` 포함

    /**
     * Address
     * 가게의 상세 주소 및 회원의 상세 주소
     */
    public static final int MAX_ADDRESS_LENGTH = 100;    // 도로명 주소 최대 28자리 + 상세 주소

    /**
     * Map
     * 지도 관련 상수
     */
    public static final int MAP_SEARCHING_RANGE_KM = 5;
    public static final int MAP_LIST_DEFAULT_LIMIT = 10;  // 무한스크롤 기본 한 페이지 크기
    public static final int MAP_LIST_MAX_LIMIT = 50;      // 무한스크롤 최대 한 페이지 크기
    /**
     * Store
     */

    public static final int MAX_STORE_NAME_LENGTH = 50;
    public static final int MAX_STORE_PHONE_LENGTH = 30;
    public static final int MAX_STORE_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_STORE_ORIGIN_LENGTH = 500;



    /**
     * Menu
     */
    public static final int MAX_MENU_NAME_LENGTH = 30;
    public static final int MAX_MENU_INFO_LENGTH = 300;

    public static final int DEFAULT_MENU_QUANTITY = 0;
    public static final int DEFAULT_MENU_SALE_PERCENT = 0;

    /**
     * Coupon
     */
    public static final int MAX_COUPON_NAME_LENGTH = 50;

    /**
     * Review
     */
    public static final int MAX_REVIEW_RATING_VALUE = 5;
    public static final int MIN_REVIEW_RATING_VALUE = 1;
    public static final int MAX_REVIEW_CONTENT_LENGTH = 2000;

    /**
     * Payment
     */
    public static final int MAX_TOSS_PAYMENT_KEY_LENGTH = 100;
    public static final int MAX_TOSS_ORDER_ID_LENGTH = 100;

    /**
     * Notification
     */
    public static final int MAX_NOTIFICATION_CONTENT_LENGTH = 255;



}


