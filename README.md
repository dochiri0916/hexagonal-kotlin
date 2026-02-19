# Base Template (Hexagonal/Clean Architecture)

이 레파지토리는 스프링 기반 애플리케이션을 위한 헥사고날/클린 아키텍처 템플릿입니다. 레이어 간 의존 규칙, 패키지 구성 규칙, 네이밍 컨벤션을 표준화하여 팀 간 일관성과 유지보수성을 높입니다.

## 아키텍처 개요

의존 방향(단방향):

```
Presentation (Web/API) → Application (UseCase) → Domain (Model)
Presentation → Application → Port(out) ← Adapter(out) ← Infrastructure
                             ^
                             └── Port(in) ← Presentation
```

핵심 원칙:
- 도메인은 어떤 외부(프레임워크, DB, 웹)에도 의존하지 않는다.
- 애플리케이션은 유스케이스(비즈니스 규칙의 절차)를 정의/구현하며, 외부 연동은 Port 인터페이스로 추상화한다.
- 프레젠테이션/인프라는 애플리케이션의 포트를 통해서만 상호작용한다.

## 패키지 구성 규칙

루트: `com.dochiri.hexagonal`

```yaml
com.dochiri.hexagonal
├─ application
│  └─ user
│     ├─ dto              # 유스케이스 입/출력 모델(Command/Query/Result)
│     ├─ port
│     │  ├─ in            # 입력 포트(유스케이스 인터페이스)
│     │  └─ out           # 출력 포트(외부 연동 인터페이스)
│     └─ service
│        ├─ command       # 상태 변경 유스케이스 구현
│        └─ query         # 조회 유스케이스 구현 및 Finder
├─ domain
│  └─ user
│     ├─ vo               # 값 객체(Email, UserId 등)
│     ├─ exception        # 도메인 예외
│     └─ User, UserRole…  # 도메인 엔티티/모델
├─ infrastructure
│  ├─ auth                # JWT, 시큐리티 설정/필터/어댑터
│  ├─ common.persistence  # JPA 공통 설정(Auditing 등)
│  ├─ security            # CORS, EntryPoint, Cookie 유틸 등
│  └─ user
│     └─ adapter
│        └─ out
│           └─ persistence # 영속 어댑터(JPA Repository/Mapper/Entity)
└─ presentation
   ├─ auth
   │  ├─ request          # 요청 DTO (Web 전용)
   │  └─ response         # 응답 DTO (Web 전용)
   └─ common
      ├─ exception        # API 예외 처리(@ControllerAdvice)
      └─ response         # 공통 API 래퍼(ApiResponse)
```

### 규칙과 이유

1) Presentation → Application 의존, 역방향 금지
- 규칙: 애플리케이션 레이어가 프레젠테이션 타입을 import 하지 않는다.
- 이유: 유스케이스는 UI에 독립적이어야 하며, 테스트 용이성과 교체 가능성(REST → gRPC 등)을 보장한다.

2) Application → Port(out) 인터페이스, Infrastructure에서 구현
- 규칙: 외부 시스템(DB/JWT/메시징)은 `port.out` 인터페이스로 추상화하고, 구현은 `infrastructure.*.adapter.out`에 둔다.
- 이유: 인프라 교체(JPA ↔ MyBatis, JWT 라이브러리 변경)가 유스케이스에 영향 주지 않도록 한다.

3) Domain은 순수 모델
- 규칙: 도메인 패키지는 스프링/영속성/웹에 의존하지 않는다.
- 이유: 도메인이 바뀌면 시스템의 핵심 의미가 바뀌므로 외부 프레임워크 변화와 분리한다.

4) DTO 배치와 역할 구분
- 규칙: 유스케이스 입출력은 `application.*.dto`에 위치한다. 컨트롤러 입출력은 `presentation.*.request/response`에 위치한다.
- 이유: 계층별 책임을 구분하여 재사용성/테스트 용이성 향상.

5) 어댑터 in/out 네이밍
- 규칙: 시스템 경계 기준으로 Web/Message 수신은 `adapter.in`, DB/외부 API 호출은 `adapter.out`으로 둔다.
- 이유: ‘들어오는/나가는’ 방향을 일관되게 표현하여 아키텍처 가시성 확보.

## 네이밍 컨벤션

### 정적 팩토리: of vs from
- 값 기반 생성: `of(...)`
  - 예: `LoginUserResult.of(id, email, role, accessToken)`
- 타입 변환(매핑): `from(SourceType)`
  - 예: `UserProfileResult.from(User user)`, `LoginUserResponse.from(LoginUserResult result)`

이유: API 의도가 명확해지고 호출부 가독성이 좋아진다. 동일 타입의 필드들로 바로 구성하면 `of`, 다른 타입에서 변환이면 `from`.

### Command/Query/Result
- 입력: `*Command`(상태 변경) / `*Query`(조회)
- 출력: `*Result`(애플리케이션 결과 모델)
- 예:
  - `RegisterUserCommand` → `RegisterUserResult`
  - `GetUserProfileQuery#getProfile` → `UserProfileResult`

### Port/Adapter
- Port 인터페이스: `application.*.port.in|out`
- Adapter 구현: `infrastructure.*.adapter.in|out.[기술명]`
- 예: `LoadUserPort`, `UserPersistenceAdapter implements LoadUserPort`

## 컨트롤러 역할

- Request DTO → Command/Query로 변환
- UseCase 실행 (Port.in)
- Result → Response DTO로 변환
- 공통 응답 래퍼(`ApiResponse.success(data)`)로 감싼다

예시:

```java
// presentation/auth/AuthController
LoginUserResult result = loginUserUseCase.login(new LoginUserCommand(request.email(), request.password()));
return ApiResponse.success(LoginUserResponse.from(result));
```

## 인프라스트럭처 가이드

- 영속 어댑터: 애플리케이션의 `port.out`만 의존, 도메인 변환은 Mapper를 통해 수행
- 보안/JWT: 토큰 생성/검증은 `JwtTokenPort`로 추상화하고, 구현은 `infrastructure.auth.adapter.out`에 둔다
- 설정/필터: 스프링/시큐리티 구성은 `infrastructure.auth|security`로 분리

## 예외/응답 규칙

- 도메인/애플리케이션 실패는 의미 있는 예외로 표현(예: `InvalidPasswordException`, `UserNotFoundException`)
- 프레젠테이션에서는 예외를 잡아 적절한 상태코드/메시지로 변환(`ApiExceptionHandler`)
- 성공 응답은 `ApiResponse<T>`로 일관되게 감싼다

## 체크리스트

- [ ] Application이 Presentation을 import 하지 않는다
- [ ] Domain이 Spring/JPA/Web을 import 하지 않는다
- [ ] Adapter 는 Port 인터페이스만 의존한다
- [ ] Request/Response 는 Presentation에만 존재한다
- [ ] Command/Query/Result 가 Application DTO에 존재한다
- [ ] 정적 팩토리 네이밍(`of`/`from`) 규칙을 따른다

## 확장 팁

- 새로운 기능 추가 시, 아래 순서 권장:
  1) Domain 모델/규칙 정의 또는 확장
  2) Port(in/out) 설계
  3) Application UseCase 구현(service.command/query)
  4) Infrastructure Adapter 구현(필요 시)
  5) Presentation Controller/Request/Response 연결

- 모듈 분리(선택): 규모가 커지면 `domain`, `application`, `infrastructure`, `presentation`을 별도 모듈로 분리하여 컴파일 타임 의존을 강제할 수 있음.

---

이 문서는 템플릿의 기본 규칙을 요약합니다. 팀 상황에 맞는 세부 컨벤션(패키지 세분화, 예외 매핑 정책, 로그 규칙 등)을 추가해 운영하세요.

---

## 레이어별로 무엇이 들어가고, 무엇이 금지되는가

아래 항목은 “포함(Allowed)”과 “금지(Forbidden)”를 명시적으로 나눠 레이어 경계를 강하게 유지하기 위한 규칙입니다.

### Presentation (Web/API)
- 포함(Allowed)
  - Controller (`@RestController`), Web 전용 Request/Response DTO
  - 입력 검증(Bean Validation 애노테이션), 인증 주체 해석(`@AuthenticationPrincipal`)
  - 결과 포맷팅(`ApiResponse<T>`), 예외 → HTTP 상태코드 매핑(`@ControllerAdvice`)
  - 애플리케이션 입력 포트(UseCase) 호출, Command/Query 생성 및 Result → Response 매핑
- 금지(Forbidden)
  - 도메인 비즈니스 규칙, 트랜잭션 경계 설정, 인프라 직접 접근(JPA/외부 API)
  - 애플리케이션/도메인 모델을 Web 스키마에 맞춰 변형 저장(표현 변환만 허용)

### Application (UseCase)
- 포함(Allowed)
  - 유스케이스 인터페이스(Port.in)와 구현(Service)
  - Command/Query/Result DTO, 트랜잭션 경계 설정(`@Transactional`)
  - 출력 포트(Port.out)를 통한 외부 연동 추상화(DB/JWT/메시지 등)
  - 도메인 모델 조립/오케스트레이션, 권한/상태 점검, 시나리오 절차 관리
- 금지(Forbidden)
  - 프레젠테이션 타입(Request/Response) 의존
  - 특정 기술(JPA Entity, Jwt 구현체 등) 의존

### Domain (Model)
- 포함(Allowed)
  - 엔티티/애그리게잇(`User`), 값 객체(VO: `Email`, `UserId`), 도메인 예외
  - 불변식/비즈니스 규칙, 상태 전이 메서드(e.g., `user.updateLastLoginAt()`)
- 금지(Forbidden)
  - 스프링/웹/JPA/보안 등 프레임워크 의존
  - I/O, 트랜잭션, DB 키 누설

### Infrastructure
- 포함(Allowed)
  - Adapter.in/out 구현(Web 제외), JPA 엔티티/레포지토리/매퍼, 보안/설정/유틸
  - 외부 시스템 연동 구현체(JWT 토큰 생성, 이메일 전송 등)
- 금지(Forbidden)
  - 애플리케이션/도메인에 대한 역방향 의존 추가(비순환 유지)
  - 유스케이스 절차/규칙 구현(비즈니스 로직 탑재 금지)

---

## 식별자 전략: publicId vs 내부 DB id

본 템플릿은 내부 DB 식별자와 외부 공개 식별자를 분리합니다.

- 내부 DB id: `UserEntity.id : Long`
  - 용도: 데이터베이스 내부 조인/성능 최적화, 변경 가능성이 있는 물리적 키
  - 절대 외부로 노출하지 않음(API 응답/로그/토큰 금지)

- 공개 식별자(publicId): `UserEntity.publicId : String`, 도메인에선 `UserId` VO
  - 생성: `UserId.newId()`가 UUID 문자열을 생성하여 최초 등록 시 고정
  - 용도: API 경로/응답/토큰 클레임/로그 상관키로 안전하게 노출
  - 규칙: 생성 후 불변, 재발급 금지(리네임이 아니라 새로운 사용자로 간주)

### 왜 분리하는가(이유)
1) 보안/정보 은닉: DB 키 패턴/증가량이 노출되면 스캐닝 공격에 취약합니다. 난수형 publicId는 추측이 어렵습니다.
2) 내구성: 물리 스키마 변경(샤딩/리플리케이션/마이그레이션)에도 publicId는 변하지 않아 외부 계약(API/링크)가 안정적입니다.
3) 경계 명확화: 토큰과 API는 publicId만 다루고, DB는 내부 id를 사용함으로써 레이어 책임이 분명해집니다.

### 사용 규칙(Do/Don’t)
- Do: 컨트롤러/응답/토큰/로그에는 publicId만 사용한다.
- Do: 애플리케이션/도메인에서는 `UserId` VO를 통해 publicId를 표현한다.
- Do: 인프라 퍼시스턴스는 `publicId`(고유 인덱스)와 `id(Long)` 모두 보유하고 매퍼로 상호 변환한다.
- Don’t: 내부 `id(Long)`를 컨트롤러/DTO/토큰/로그에 노출하지 않는다.
- Don’t: publicId를 재발급하지 않는다(식별자 불변 원칙 위배).

### 현재 구현과의 매핑
- Entity: `UserEntity { Long id; String publicId; ... }`
- Domain VO: `UserId(value: String)` → `UserEntity.publicId`
- 토큰: `JwtTokenPort.generateAccessToken(user.getId().value(), role)` → `sub`는 publicId
- 조회: `UserJpaRepository.findByPublicId(String)`로 도메인 복원

인덱스 권장: `users.public_id`에 유니크 인덱스(이미 unique=true) + prefix 인덱싱 고려(엔진별).

---

## 값 객체(VO)를 쓰는 이유와 규칙

본 템플릿은 도메인 핵심 개념을 값 객체로 캡슐화합니다. 예: `Email`, `UserId`.

### 왜 VO인가
1) 불변성(immutability): 생성 시 검증 후 변하지 않음 → 스레드 안전/사이드이펙트 감소.
2) 자체 검증(invariants): `Email.from(String)`에서 형식/정규화(트리밍/소문자화)를 보장.
3) 타입 안전(type-safety): `String` 남용으로 인한 파라미터 순서/의미 오류 방지.
4) 동등성 의미: 값 기준 비교가 자연스러움(레코드/equals).
5) 중복 제거: 검증 로직이 한 곳(VO)에서 재사용.

### 생성/사용 규칙
- 모든 외부에서 들어오는 문자열은 가능한 VO로 전환한 뒤 도메인/애플리케이션에서 사용한다.
- VO는 생성자/팩토리에서 유효성 검사를 수행한다.
- VO ↔ DTO/Entity 변환은 경계에서만 수행한다.
- VO는 비즈니스 행위를 담을 수 있으나, I/O는 금지한다.

### 예시(현재 코드)
- `Email`
  - 정규식 검증, 트리밍, 소문자화 → 일관 저장/비교
- `UserId`
  - `newId()`로 UUID 생성 → 도메인에서 식별자 생성 책임 보유

---

## DTO와 매핑 규칙(경계에서 변환)

- Application DTO: `Command/Query/Result`는 유스케이스 입/출력 모델
  - 입력값 원천은 Presentation이지만, 변환 후에는 Presentation과 분리된 모델을 사용
- Presentation DTO: Request/Response는 웹 스키마/문맥에 맞춘 표현 담당
- 변환 위치
  - Request → Command/Query: Controller
  - Result → Response: Controller
  - Domain ↔ Result: Application Service(예: `UserProfileResult.from(User)`)
  - Domain ↔ Entity: Infrastructure Mapper

### 정적 팩토리 네이밍 재확인
- 값 기반: `of(...)`
- 타입 변환: `from(Type)`

---

## 포트/어댑터 심화 가이드

- 입력 포트(Port.in): 유스케이스 인터페이스. 예) `LoginUserUseCase#login`
- 출력 포트(Port.out): 외부 연동 추상화. 예) `LoadUserPort`, `JwtTokenPort`
- 어댑터 구현 위치: `infrastructure.*.adapter.in|out.[기술명]`
- 의존 방향: Application → Port(out) ← Adapter(out) ← Infrastructure
- 새 연동 추가 절차
  1) Port(out) 정의(필요한 계약 최소화)
  2) Application에서 Port 사용
  3) Infrastructure에서 Adapter 구현 및 Bean 등록

도메인/애플리케이션은 기술 교체와 무관해야 함(JWT 라이브러리, JPA ↔ MyBatis 교체 등).

---

## 퍼시스턴스/매핑/트랜잭션

- 엔티티 vs 도메인 분리
  - Entity는 JPA 애노테이션/지연로딩 등을 포함(프레임워크 종속)
  - 도메인은 순수 POJO/VO/규칙만 포함(프레임워크 독립)
- 매핑 계층: `UserMapper`가 Entity ↔ Domain 변환/부분 업데이트 책임
- 저장 규칙: 존재 확인 후 신규/갱신 분기(`save`에서 upsert 스타일), `updateLastLoginAt()` 같은 변경은 Entity 메서드로 캡슐화
- 트랜잭션 경계: 애플리케이션 서비스 레벨에서 명시(`@Transactional`), 읽기/쓰기 분리 고려 가능
- 감사(Auditing): `BaseEntity` + `AuditingConfig`로 생성/수정 시각/주체 관리

---

## 보안/JWT 설계

- 추상화: `JwtTokenPort`로 토큰 생성/검증을 숨김
- 어댑터: `infrastructure.auth.adapter.out.JwtTokenAdapter`가 구현
- 필터: `infrastructure.auth.adapter.in.JwtAuthenticationFilter`가 요청을 가로채 인증 주체 설정
- 클레임 권장
  - `sub`: 사용자 publicId
  - `role`: 권한(Role)
  - `iat/exp`: 발급/만료
- 컨트롤러에서의 주체 사용: `@AuthenticationPrincipal String userPublicId`
- 응답: 로그인 성공 시 `LoginUserResponse`에 `accessToken` 포함

---

## 검증/에러 처리 정책

- 검증(Validation)
  - Presentation: 형식/존재성(Bean Validation)
  - Domain/VO: 본질적 불변식(이메일 형식, 공백 금지 등)
  - Application: 권한/상태/흐름(활성 사용자, 비밀번호 일치 등)
- 예외 → 상태코드 매핑(권장)
  - `InvalidPasswordException` → 401 Unauthorized
  - `InactiveUserException` → 403 Forbidden
  - `UserNotFoundException` → 404 Not Found
  - `DuplicateEmailException` → 409 Conflict
  - 기타 유효성 실패 → 400 Bad Request
- 응답 포맷: 성공은 `ApiResponse<T>`, 에러는 `ApiExceptionHandler`에서 표준 형태로 변환

---

## 테스트 전략

- Domain: 순수 단위 테스트(VO/엔티티 불변식/행위)
- Application: 포트를 mock하여 유스케이스 시나리오 테스트(트랜잭션/예외 흐름 포함)
- Infrastructure: 어댑터/매퍼 통합 테스트(JPA 슬라이스/컨테이너)
- Presentation: 컨트롤러 슬라이스 테스트(MockMvc), 예외 매핑/검증 확인
- 계약/회귀: 포트 인터페이스 기준 계약 테스트(필요 시)