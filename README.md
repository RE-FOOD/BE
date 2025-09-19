# RE:FOOD
🥇 2025 IITP 우수성과공유 컨퍼런스 최우수상

<img width="1062" height="599" alt="image" src="https://github.com/user-attachments/assets/b61ab463-3957-49d2-b152-948a86699d2b" />


## 🌳 서비스 소개

**가게에서 버려지는 식품들 너무 아까운데... 내가 싸게 살 수는 없을까?**   

리푸드는 남는 식품 거래 플랫폼 서비스로, 합리적 소비와 환경보호를 모두 얻는 똑똑한 푸드 픽업 서비스입니다.  
이를 통해 합리적이고 환경친화적인 식문화에 기여하자! 라는 서비스 목표를 갖고 있습니다.  



### 💰 우리 서비스의 가치
- **경제적 가치 - 소비자** : 소비자는 근처 마감 할인을 통한 이득을 얻는 동시에, 환경 포인트 및 쿠폰 리워드를 통해 서비스 이용 동기를 얻습니다.
- **경제적 가치 - 판매자** : 판매자는 재고에 대한 원가를 회수하고, 빠른 가게 노출을 통한 추가 매출 기회를 얻습니다.  
- **환경 보호**: 음식물 쓰레기 감소를 통한 탄소 배출량 감축

## ✨ 주요 기능

### 🔐 인증 및 권한 관리
- **JWT 기반 인증 시스템**: 안전하고 확장 가능한 토큰 기반 인증
- **OAuth2 소셜 로그인**: 카카오 로그인 지원
- **역할별 권한 처리**: 일반 사용자, 사업자, 관리자 역할 구분
- **환경 포인트 시스템**: 환경 친화적 활동에 대한 포인트 적립

### 🏪 가게 관리
- **사업자 등록 및 승인**: 사업자 자격 검증 시스템
- **가게 정보 관리**: 메뉴, 영업시간, 위치 등 상세 정보 관리
- **실시간 재고 관리**: 남는 음식 실시간 업데이트

### 🗺️ 지도 기반 서비스
- **위치 기반 가게 조회**: 사용자 위치 기반 근처 가게 검색
- **거리 계산**: 사용자와 가게 간 거리 자동 계산
- **지역별 필터링**: 원하는 지역의 가게만 조회

### 🛒 주문 및 결제
- **장바구니 시스템**: Redis 기반 실시간 장바구니 관리
- **주문 프로세스**: 주문 생성, 확인, 취소 전체 플로우
- **결제 시스템**: 다양한 결제 수단 지원
- **주문 내역 관리**: 사용자별 주문 이력 조회

### 🔔 알림 서비스
- **FCM 푸시 알림**: 실시간 주문 상태 알림
- **환경 레벨 업 알림**: 포인트 적립 및 레벨 상승 알림
- **가게별 알림**: 새로운 메뉴, 할인 정보 등


## 🛠️ 기술 스택

<img width="907" height="464" alt="2025-09-05_10 51 55" src="https://github.com/user-attachments/assets/8672fc64-0f1b-423c-97be-e6c8fb3158c2" />


### Backend
- Java 21 LTS
- Spring Boot 3.5.4, Spring Security, Spring Data JPA

### Database
- MySQL 8.0.43
- Redis 8.2.0

### Query & ORM
- QueryDSL 5.0.0
- JPA/Hibernate

### External Services
- AWS S3
- Kakao API, Toss Payments API
- FCM(Firebase Cloud Messaging)
- Spring Mail

### Development Tools
- Gradle
- Docker compose & github actions : 서버 컨테이너화를  통한 CI/CD  
- Swagger


### Other Dependencies
- JWT, Lombok, Jackson, thymeleaf


## 📁 프로젝트 구조

```
src/main/java/com/iitp/
├── domains/                    # 도메인별 모듈
│   ├── member/                 # 회원 관리 도메인
│   │   ├── controller/         # 회원 관련 API
│   │   ├── domain/             # 도메인 모델
│   │   │   ├── entity/         # 엔티티
│   │   ├── dto/                # 요청/응답 DTO
│   │   ├── repository/         # 데이터 접근 계층
│   │   └── service/            # 회원 비즈니스 로직
│   ├── store/
│   ├── ...
|
├── global/                     # 공통 모듈
│   ├── config/                 # 설정
│   ├── exception/              # 예외 처리
│   ├── jwt/                    # JWT 관련
│   ├── redis/                  # Redis 서비스
│   └── util/                   # 유틸리티
└── IitpApplication.java        # 메인 애플리케이션 클래스
```

## 🏗️ 시스템 아키텍처

<img width="1125" height="551" alt="image" src="https://github.com/user-attachments/assets/8e3643c6-3abb-4686-9e69-d05441293365" />


### CQRS (Command Query Responsibility Segregation) 패턴
- **명령과 조회 분리**: 읽기와 쓰기 작업의 최적화
- **Command**: 데이터 변경 작업 (Create, Update, Delete)
- **Query**: 인메모리 DB Redis를 통한 데이터 조회 작업 (Read)


## 🚀 실행 방법

### Prerequisites
- Java 21+
- MySQL 8.0+
- Redis 6.0+
- Docker (선택사항)

### Local Development
```bash
# 1. 저장소 클론
git clone <repository-url>
cd BE

# 2. 의존성 설치
./gradlew build

# 3. 데이터베이스 설정
# MySQL에 refood 데이터베이스 생성

# 4. Redis 실행
redis-server

# 5. 애플리케이션 실행
./gradlew bootRun
```

---

**Refood Team** - 환경과 함께하는 똑똑한 한 끼를 만들어갑니다 🌱
