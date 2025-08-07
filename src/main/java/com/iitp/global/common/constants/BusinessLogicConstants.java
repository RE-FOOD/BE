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
     * 각 레벨당 요구 포인트는 다음 레벨로 가기 위해 필요한 포인트를 의미함.
     * 각 레벨당 요구 포인트는 누적이 아니라 단순 합계.
     * ex. 총 주문 메뉴 1개 => 1*10 = 10 포인트  => 2레벨 0포인트
     * ex. 총 주문 메뉴 5개 => 5*10 = 50 포인트  => 3레벨 20포인트
     */
    public static final int ENVIRONMENT_LEVEL_ONE_REQUIRED_POINT = 10;
    public static final int ENVIRONMENT_LEVEL_TWO_REQUIRED_POINT = 30;
    public static final int ENVIRONMENT_LEVEL_THREE_REQUIRED_POINT = 70;
    public static final int ENVIRONMENT_LEVEL_FOUR_REQUIRED_POINT = 150;

    public static final int ENVIRONMENT_POINT_PER_ORDERED_MENU = 10;
    public static final int ENVIRONMENT_POINT_PER_REUSING_CONTAINER = 2;

    public static final double SAVED_TREE_PER_ORDERED_MENU = 0.1;
    public static final double SAVED_TREE_PER_REUSING_CONTAINER = 0.02;

    public static final double SAVED_CARBON_KG_PER_ORDERED_MENU = 1.25;
    public static final double SAVED_CARBON_KG_PER_REUSING_CONTAINER = 0.17;

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


