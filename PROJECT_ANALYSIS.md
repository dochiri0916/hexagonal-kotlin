# Hexagonal Architecture 프로젝트 코드 분석

## 1. 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **프로젝트명** | hexagonal-kotlin |
| **패키지** | `com.dochiri.hexagonal` |
| **아키텍처** | 헥사고날 아키텍처 (Ports & Adapters) |
| **언어** | Java 25 |
| **프레임워크** | Spring Boot 4.0.2 |
| **빌드 도구** | Gradle |
| **데이터베이스** | H2 (인메모리) |
| **인증 방식** | JWT (Bearer Token + Cookie) |
| **총 소스 파일** | 44개 Java 파일 |

---

## 2. 디렉토리 구조

```
src/main/java/com/dochiri/hexagonal/
├── HexagonalApplication.java                    # 애플리케이션 진입점
│
├── domain/                                       # 도메인 계층 (순수 비즈니스 로직)
│   └── user/
│       ├── User.java                             # 도메인 엔티티 (Aggregate Root)
│       ├── UserRole.java                         # 역할 enum (USER, ADMIN)
│       ├── UserStatus.java                       # 상태 enum (ACTIVE, INACTIVE, DELETED)
│       ├── vo/
│       │   ├── Email.java                        # 이메일 Value Object
│       │   └── UserId.java                       # 사용자 ID Value Object
│       └── exception/
│           ├── DuplicateEmailException.java
│           ├── InactiveUserException.java
│           ├── InvalidPasswordException.java
│           └── UserNotFoundException.java
│
├── application/                                  # 애플리케이션 계층 (유스케이스)
│   └── user/
│       ├── dto/
│       │   ├── RegisterUserCommand.java          # 회원가입 입력
│       │   ├── RegisterUserResult.java           # 회원가입 결과
│       │   ├── LoginUserCommand.java             # 로그인 입력
│       │   ├── LoginUserResult.java              # 로그인 결과
│       │   └── UserProfileResult.java            # 프로필 조회 결과
│       ├── port/
│       │   ├── in/                               # 인바운드 포트 (유스케이스 인터페이스)
│       │   │   ├── RegisterUserUseCase.java
│       │   │   ├── LoginUserUseCase.java
│       │   │   └── GetUserProfileQuery.java
│       │   └── out/                              # 아웃바운드 포트 (외부 의존성 인터페이스)
│       │       ├── SaveUserPort.java
│       │       ├── LoadUserPort.java
│       │       ├── UpdateUserLoginPort.java
│       │       └── JwtTokenPort.java
│       └── service/
│           ├── command/                          # 명령 서비스 (CQS 패턴)
│           │   ├── RegisterUserService.java
│           │   └── LoginUserService.java
│           └── query/                            # 조회 서비스
│               ├── GetUserProfileService.java
│               └── UserFinder.java
│
├── infrastructure/                               # 인프라스트럭처 계층 (기술 구현)
│   ├── auth/
│   │   ├── adapter/
│   │   │   ├── in/
│   │   │   │   └── JwtAuthenticationFilter.java  # JWT 인증 필터
│   │   │   └── out/
│   │   │       └── JwtTokenAdapter.java          # JWT 토큰 생성/검증
│   │   └── config/
│   │       ├── JwtProperties.java                # JWT 설정 프로퍼티
│   │       └── SecurityConfig.java               # Spring Security 설정
│   ├── common/persistence/
│   │   ├── AuditingConfig.java                   # JPA Auditing 활성화
│   │   ├── AuditorAwareImpl.java                 # 현재 사용자 감사 정보
│   │   └── BaseEntity.java                       # 공통 엔티티 (생성일/수정일)
│   ├── security/
│   │   ├── CookieProperties.java                 # 쿠키 설정
│   │   ├── CorsProperties.java                   # CORS 설정
│   │   ├── JwtAuthenticationEntryPoint.java      # 401 처리
│   │   ├── JwtAccessDeniedHandler.java           # 403 처리
│   │   └── util/
│   │       └── CookieUtil.java                   # 쿠키 유틸리티
│   └── user/
│       ├── adapter/out/persistence/
│       │   ├── UserJpaRepository.java            # Spring Data JPA Repository
│       │   └── UserPersistenceAdapter.java       # 영속성 어댑터
│       ├── entity/
│       │   └── UserEntity.java                   # JPA 엔티티
│       └── mapper/
│           └── UserMapper.java                   # Domain ↔ Entity 변환
│
└── presentation/                                 # 프레젠테이션 계층 (API)
    ├── auth/
    │   ├── AuthController.java                   # REST 컨트롤러
    │   ├── request/
    │   │   ├── RegisterUserRequest.java
    │   │   └── LoginUserRequest.java
    │   └── response/
    │       ├── RegisterUserResponse.java
    │       ├── LoginUserResponse.java
    │       └── UserProfileResponse.java
    └── common/
        ├── exception/
        │   ├── BaseExceptionHandler.java         # 예외 처리 기반 클래스
        │   └── ApiExceptionHandler.java          # 글로벌 예외 핸들러
        └── response/
            └── ApiResponse.java                  # 공통 API 응답 래퍼
```

---

## 3. 아키텍처 분석

### 3.1 의존성 방향

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                      │
│              (Controller, Request/Response)              │
└──────────────────────┬──────────────────────────────────┘
                       │ depends on
                       ▼
┌─────────────────────────────────────────────────────────┐
│                  Application Layer                       │
│           (UseCase, Port, Service, DTO)                  │
└──────────┬──────────────────────────────┬───────────────┘
           │ depends on                   │ defines port
           ▼                              ▼
┌──────────────────────┐   ┌──────────────────────────────┐
│    Domain Layer      │   │   Infrastructure Layer        │
│ (Entity, VO, Enum)   │   │ (Adapter, Config, Entity)     │
│   순수 비즈니스 로직    │   │   포트 인터페이스 구현          │
└──────────────────────┘   └──────────────────────────────┘
```

**핵심 원칙**: 의존성이 항상 안쪽(Domain)을 향하며, Domain 계층은 외부 프레임워크에 전혀 의존하지 않는다.

### 3.2 포트와 어댑터 매핑

| 포트 (인터페이스) | 어댑터 (구현체) | 방향 |
|---|---|---|
| `RegisterUserUseCase` | `RegisterUserService` | Inbound (Driving) |
| `LoginUserUseCase` | `LoginUserService` | Inbound (Driving) |
| `GetUserProfileQuery` | `GetUserProfileService` | Inbound (Driving) |
| `SaveUserPort` | `UserPersistenceAdapter` | Outbound (Driven) |
| `LoadUserPort` | `UserPersistenceAdapter` | Outbound (Driven) |
| `UpdateUserLoginPort` | `UserPersistenceAdapter` | Outbound (Driven) |
| `JwtTokenPort` | `JwtTokenAdapter` | Outbound (Driven) |

---

## 4. 계층별 상세 분석

### 4.1 Domain 계층

프레임워크 의존성이 전혀 없는 순수 Java 코드로 구성된다.

#### User (Aggregate Root)

```java
@Getter
public class User {
    private final UserId id;
    private final Email email;
    private final String passwordHash;
    private final String name;
    private final UserStatus status;
    private final UserRole role;
    private final LocalDateTime lastLoginAt;
}
```

- 모든 필드가 `final`로 불변성 보장
- `register()` 팩토리 메서드로 신규 사용자 생성
- `of()` 팩토리 메서드로 기존 데이터 복원
- `requireNonNull`로 필수 값 검증

#### Value Objects

**Email** - `record` 기반, 정규식 검증 + 소문자 정규화
```java
public record Email(String value) {
    public Email {
        // 정규식 패턴 매칭 및 trim/lowercase 정규화
    }
}
```

**UserId** - `record` 기반, UUID 자동 생성
```java
public record UserId(String value) {
    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }
}
```

#### Domain Exceptions

| 예외 클래스 | HTTP 매핑 | 설명 |
|---|---|---|
| `UserNotFoundException` | 404 NOT_FOUND | 사용자 조회 실패 |
| `DuplicateEmailException` | 409 CONFLICT | 이메일 중복 |
| `InvalidPasswordException` | 400 BAD_REQUEST | 비밀번호 불일치 |
| `InactiveUserException` | 403 FORBIDDEN | 비활성 계정 |

---

### 4.2 Application 계층

#### CQS(Command-Query Separation) 패턴 적용

**Command 서비스** - 상태를 변경하는 작업

- `RegisterUserService`: 이메일 중복 검사 → 비밀번호 암호화 → 사용자 생성/저장
- `LoginUserService`: 사용자 조회 → 비밀번호 검증 → 활성 상태 확인 → JWT 발급

**Query 서비스** - 데이터를 조회만 하는 작업

- `GetUserProfileService`: UserId로 사용자 조회 → 프로필 DTO 반환
- `UserFinder`: 공통 사용자 조회 로직 (id/email 기반)

#### DTO 흐름

```
[Request DTO] → [Command/Query DTO] → [Service] → [Result DTO] → [Response DTO]

RegisterUserRequest → RegisterUserCommand → RegisterUserService → RegisterUserResult → RegisterUserResponse
LoginUserRequest    → LoginUserCommand    → LoginUserService    → LoginUserResult    → LoginUserResponse
```

각 계층의 DTO를 분리하여 계층 간 결합도를 최소화한다.

---

### 4.3 Infrastructure 계층

#### 영속성 (Persistence)

**UserEntity** - JPA 엔티티로 DB 테이블 매핑
- 내부 식별자 `id` (Long, auto-increment) + 외부 식별자 `publicId` (UUID)
- `BaseEntity` 상속으로 `createdAt`, `updatedAt`, `createdBy`, `updatedBy` 자동 관리

**UserPersistenceAdapter** - 3개의 아웃바운드 포트를 단일 클래스에서 구현
- `LoadUserPort`: publicId 또는 email로 사용자 조회
- `SaveUserPort`: 신규 생성 또는 기존 업데이트 (Upsert 패턴)
- `UpdateUserLoginPort`: 최종 로그인 시간 갱신

**UserMapper** - Domain ↔ Entity 양방향 변환
- `toEntity()`: Domain → JPA Entity
- `toDomain()`: JPA Entity → Domain
- `applyFullUpdate()`: Domain 변경사항을 기존 Entity에 적용

#### 인증 (Authentication)

**JwtTokenAdapter** - JJWT 라이브러리 기반 JWT 처리
- Access Token / Refresh Token 분리 (`category` 클레임)
- HMAC-SHA 서명
- 토큰 생성, 검증, 파싱, 만료 확인

**JwtAuthenticationFilter** - `OncePerRequestFilter` 확장
- `Authorization: Bearer {token}` 헤더에서 토큰 추출
- 토큰 유효성 + Access Token 여부 검증
- `SecurityContextHolder`에 인증 정보 설정

#### 보안 설정 (SecurityConfig)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
    .anyRequest().authenticated()
)
```

- CSRF 비활성화 (Stateless API)
- 세션 미사용 (`STATELESS`)
- BCrypt 비밀번호 인코더
- CORS 허용 출처: `http://localhost:3000`

---

### 4.4 Presentation 계층

#### API 엔드포인트

| Method | URL | 인증 | 설명 |
|---|---|---|---|
| `POST` | `/api/auth/register` | 불필요 | 회원가입 |
| `POST` | `/api/auth/login` | 불필요 | 로그인 |
| `GET` | `/api/auth/me` | 필요 (Bearer) | 내 프로필 조회 |

#### 공통 응답 형식 (ApiResponse)

```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-02-19T10:00:00"
}
```

- `@JsonInclude(NON_NULL)` 적용으로 null 필드 제외

#### 예외 처리 (RFC 7807 ProblemDetail)

`BaseExceptionHandler`를 상속한 `ApiExceptionHandler`가 `@ControllerAdvice`로 전역 예외를 처리한다.

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "사용자를 찾을 수 없습니다: ...",
  "timestamp": "2026-02-19T10:00:00",
  "exception": "UserNotFoundException",
  "path": "/api/auth/me"
}
```

- 5xx 에러: `log.error()` + 스택트레이스
- 4xx 에러: `log.warn()` + 스택트레이스

---

## 5. 주요 비즈니스 흐름

### 5.1 회원가입 흐름

```
Client → POST /api/auth/register
  → AuthController.register()
    → RegisterUserRequest (유효성 검증: @Email, @NotNull)
    → RegisterUserCommand 생성
    → RegisterUserService.register()
      → Email Value Object 생성 (형식 검증)
      → LoadUserPort.loadByEmail() (중복 검사)
      → PasswordEncoder.encode() (BCrypt 암호화)
      → User.register() (도메인 엔티티 생성)
      → SaveUserPort.save() → UserPersistenceAdapter
        → UserMapper.toEntity() → JPA save
      → RegisterUserResult 반환
    → RegisterUserResponse 변환
  → ApiResponse.success() 래핑
← 200 OK
```

### 5.2 로그인 흐름

```
Client → POST /api/auth/login
  → AuthController.login()
    → LoginUserCommand 생성
    → LoginUserService.login()
      → UserFinder.findByEmail() (사용자 조회)
      → PasswordEncoder.matches() (비밀번호 검증)
      → UserStatus 확인 (ACTIVE 여부)
      → UpdateUserLoginPort.updateLastLoginAt()
      → JwtTokenPort.generateAccessToken()
      → LoginUserResult 반환 (id, email, role, accessToken)
    → LoginUserResponse 변환
  → ApiResponse.success() 래핑
← 200 OK
```

### 5.3 프로필 조회 흐름

```
Client → GET /api/auth/me (Authorization: Bearer {token})
  → JwtAuthenticationFilter
    → 토큰 추출 및 검증
    → SecurityContext에 userPublicId 설정
  → AuthController.getProfile(@AuthenticationPrincipal)
    → GetUserProfileService.getProfile()
      → UserFinder.findById()
      → UserProfileResult 반환
    → UserProfileResponse 변환
  → ApiResponse.success() 래핑
← 200 OK
```

---

## 6. 설정 정보 (application.yml)

| 설정 | 값 |
|---|---|
| 서버 포트 | `8888` |
| DB URL | `jdbc:h2:mem:testdb` |
| DDL 전략 | `create-drop` |
| H2 콘솔 | `/h2-console` (활성화) |
| JWT Access Token 만료 | 1시간 (3,600,000ms) |
| JWT Refresh Token 만료 | 14일 (1,209,600,000ms) |
| 쿠키 HttpOnly | `true` |
| 쿠키 Secure | `true` |
| 쿠키 SameSite | `None` |
| CORS 허용 출처 | `http://localhost:3000` |
| Jackson 타임존 | `Asia/Seoul` |

---

## 7. 의존성 목록

| 라이브러리 | 용도 |
|---|---|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-validation` | Bean Validation (@Valid) |
| `jjwt-api:0.12.6` | JWT 토큰 처리 |
| `lombok` | 보일러플레이트 코드 제거 |
| `h2` | 인메모리 데이터베이스 |

---

## 8. 아키텍처 특징 및 설계 패턴

| 패턴 | 적용 위치 | 설명 |
|---|---|---|
| **헥사고날 아키텍처** | 전체 구조 | Port/Adapter로 내외부 분리 |
| **CQS** | Application Service | Command/Query 서비스 분리 |
| **Value Object** | `Email`, `UserId` | 불변, 자기 검증, record 기반 |
| **팩토리 메서드** | `User.register()`, `User.of()` | 생성 로직 캡슐화 |
| **Mapper 패턴** | `UserMapper` | 도메인 ↔ 영속성 모델 분리 |
| **DTO 계층 분리** | Request → Command → Result → Response | 계층 간 결합도 최소화 |
| **RFC 7807** | `ProblemDetail` | 표준화된 에러 응답 |
| **JPA Auditing** | `BaseEntity` | 생성/수정 시간 및 사용자 자동 기록 |
| **내부/외부 ID 분리** | `UserEntity.id` / `publicId` | 내부 PK 노출 방지 |

---

## 9. 발견된 이슈

### 9.1 `User.updateLastLoginAt()` 버그

```java
// domain/user/User.java
public void updateLastLoginAt() {
    new User(id, email, passwordHash, name, status, role, LocalDateTime.now());
    // ← 새 객체를 생성만 하고 반환하지 않음. 기존 객체의 상태는 변경되지 않음.
}
```

`User`는 불변 객체이므로 `updateLastLoginAt()`은 새로운 `User` 인스턴스를 **반환**해야 한다. 현재 코드는 생성된 객체를 버리고 있어 실질적으로 아무 동작도 하지 않는다.

**수정 제안**:
```java
public User updateLastLoginAt() {
    return new User(id, email, passwordHash, name, status, role, LocalDateTime.now());
}
```

### 9.2 프로젝트명과 실제 언어 불일치

프로젝트명은 `hexagonal-kotlin`이지만 실제 코드는 **Java**로 작성되어 있다.
