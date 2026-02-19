# Kotlin Hexagonal Architecture 개선 포인트

> Java → Kotlin 마이그레이션 후, DDD · Hexagonal · Clean Architecture · 대기업 실무 · 코틀린 관용구 기준으로 정리

---

## 1. 도메인 레이어

### 1-1. `UserRepository`를 도메인에서 분리 — Out Port로 이동

**현재**: `domain/user/UserRepository.kt`에 리포지토리 인터페이스가 위치
**문제**: Hexagonal Architecture에서 도메인은 **외부 의존 방향을 모르는** 순수 계층이어야 한다. 리포지토리는 영속성이라는 외부 관심사에 대한 **out port**이다.
**개선**: `application/user/port/out/UserRepository.kt`로 이동하거나, 기존의 `LoadUserPort` / `SaveUserPort`처럼 **역할별 port**로 분리

```kotlin
// application/user/port/out/LoadUserPort.kt
interface LoadUserPort {
    fun findById(userId: UserId): User?
    fun findByEmail(email: Email): User?
}

// application/user/port/out/SaveUserPort.kt
interface SaveUserPort {
    fun save(user: User): User
}
```

> **왜?**: 서비스가 `UserRepository`라는 범용 인터페이스 대신 **필요한 기능만** 의존하게 되어 ISP(인터페이스 분리 원칙)를 지킨다.

---

### 1-2. `Optional` 대신 Kotlin Nullable 사용

**현재**: `UserRepository`가 `Optional<User>`을 반환
**문제**: `Optional`은 Java의 null-safety 부재를 보완하는 래퍼. Kotlin에는 `?` 타입 시스템이 있으므로 불필요하다.
**개선**:

```kotlin
interface LoadUserPort {
    fun findById(userId: UserId): User?
    fun findByEmail(email: Email): User?
}
```

```kotlin
// UserFinder
fun findById(userId: UserId): User =
    loadUserPort.findById(userId) ?: throw UserNotFoundException.byId(userId)
```

> `orElseThrow` → 엘비스 연산자(`?:`)로 코틀린다워진다.

---

### 1-3. `User` 도메인 객체에 `copy` 대신 `private constructor` 불변 객체 유지 — 개선 가능

**현재**: `updateLastLoginAt()`에서 모든 필드를 수동 나열하여 새 인스턴스 생성
**문제**: 필드가 추가될 때마다 모든 팩토리/업데이트 메서드를 수정해야 한다.
**개선 옵션**: 내부적으로 `data class`의 `copy()`를 활용하되, 외부 노출은 제한

```kotlin
data class User private constructor(
    val id: UserId,
    val email: Email,
    val passwordHash: String,
    val name: String,
    val status: UserStatus,
    val role: UserRole,
    val lastLoginAt: LocalDateTime?
) {
    fun updateLastLoginAt(now: LocalDateTime = LocalDateTime.now()): User =
        copy(lastLoginAt = now)

    companion object {
        fun register(...): User = User(...)
        fun reconstitute(...): User = User(...)
    }
}
```

> `data class`의 `copy()`는 `private constructor`여도 **같은 클래스 내부**에서 사용 가능하다.

---

### 1-4. `@JvmStatic`, `@JvmOverloads` 제거

**현재**: `LoginUserResult.of()`, `RegisterUserResult.of()`, `UserNotFoundException.byId()` 등에 `@JvmStatic` 부착
**문제**: 순수 Kotlin 프로젝트에서는 Java 상호 운용 어노테이션이 불필요한 노이즈
**개선**: 모든 `@JvmStatic`, `@JvmOverloads` 제거

---

### 1-5. Result DTO의 `companion object` 팩토리 — 확장 함수로 대체 가능

**현재**:
```kotlin
data class UserProfileResult(...) {
    companion object {
        fun from(user: User): UserProfileResult = ...
    }
}
```

**개선**: 매핑 로직이 단순하면 **확장 함수** 또는 **최상위 함수**로 분리하여 DTO가 도메인을 import하지 않게 할 수 있다.

```kotlin
// application/user/dto/UserProfileResultMapper.kt (또는 서비스 내부)
fun User.toProfileResult(): UserProfileResult =
    UserProfileResult(id.value, email.value, name, role.name)
```

> 도메인 → DTO 방향의 의존은 application 레이어에서만 발생해야 하며, DTO 자체가 도메인을 알 필요가 없다.

---

## 2. 애플리케이션 레이어

### 2-1. `LoginUserService`에서 `UserRepository`와 `UserFinder` 이중 의존

**현재**: `LoginUserService`가 `UserFinder`(조회)와 `UserRepository`(저장)를 **둘 다** 주입받음
**문제**: 조회는 `UserFinder`로 위임하면서 저장은 직접 `userRepository.save()` 호출 — 일관성 부족
**개선**: Port를 역할별로 분리하면 `LoginUserService`는 `LoadUserPort` + `SaveUserPort` + `JwtTokenPort` + `PasswordHashPort`만 주입

```kotlin
@Service
class LoginUserService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val passwordHashPort: PasswordHashPort,
    private val jwtTokenPort: JwtTokenPort
) : LoginUserUseCase { ... }
```

---

### 2-2. `RegisterUserService` — `ifPresent`를 코틀린 관용구로

**현재**:
```kotlin
userRepository.findByEmail(email)
    .ifPresent { throw DuplicateEmailException(email) }
```

**개선** (Nullable 전환 후):
```kotlin
loadUserPort.findByEmail(email)?.let { throw DuplicateEmailException(email) }
// 또는 도메인 서비스/정책으로 추출
```

---

### 2-3. `LoginUserCommand`에 VO 적용 검토

**현재**: `LoginUserCommand(email: String, password: String)` — 원시 타입
**검토**: Command에서 이미 `Email` VO를 사용하면 서비스에서 `Email.from()` 변환 로직을 제거할 수 있다.
**트레이드오프**: 프레젠테이션 레이어가 VO를 알아야 하므로, **서비스 진입점에서 변환**하는 현재 방식도 합리적. 팀 컨벤션에 따라 결정.

---

## 3. 인프라스트럭처 레이어

### 3-1. `UserEntity` — `lateinit var` + 빈 생성자 개선

**현재**: JPA를 위해 빈 생성자 + `lateinit var` + `private set` 패턴 사용
**문제**: `lateinit`은 초기화 전 접근 시 런타임 에러, 보일러플레이트가 많음
**개선**: `kotlin-jpa` 플러그인(이미 적용됨)이 no-arg 생성자를 자동 생성하므로, 주 생성자로 통합 가능

```kotlin
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 36)
    val publicId: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var role: String,

    @Column(nullable = false)
    var status: String,

    var lastLoginAt: LocalDateTime? = null
) : BaseEntity()
```

> 변경 가능한 필드만 `var`, 식별자는 `val`. 보조 생성자 + `lateinit` 제거.

---

### 3-2. `BaseEntity` — `@field:` 어노테이션 use-site target 누락 가능성

**현재**: `@field:CreatedDate`, `@field:Column` 등 올바르게 사용
**확인**: `var` 프로퍼티에 `protected set`을 사용하고 있는데, Kotlin에서는 **backing field**에 직접 접근하므로 정상 동작. 다만 `@MappedSuperclass`와 `open` 키워드 관련하여 `allOpen` 플러그인이 JPA 엔티티를 자동으로 열어주는지 확인 필요.

**확인 사항**: `plugin.jpa`는 `@Entity`, `@Embeddable`, `@MappedSuperclass` 클래스에 대해 자동 `open` 처리하므로 현재 설정으로 충분.

---

### 3-3. `UserMapper` — 확장 함수 또는 최상위 함수로 변환

**현재**: `@Component`로 등록된 `UserMapper` 클래스
**문제**: 상태가 없는 순수 매핑 로직에 DI 컨테이너를 사용할 필요 없음
**개선**:

```kotlin
// infrastructure/user/mapper/UserMapper.kt
fun User.toEntity(): UserEntity = UserEntity(
    publicId = id.value,
    email = email.value,
    ...
)

fun UserEntity.toDomain(): User = User.reconstitute(
    UserId.from(publicId),
    Email.from(email),
    ...
)
```

> `@Component` 제거, 확장 함수로 변환하면 `userMapper.toDomain(entity)` → `entity.toDomain()`으로 더 자연스러움.

---

### 3-4. `UserPersistenceAdapter.save()` — Upsert 로직 개선

**현재**: `findByPublicId`로 존재 여부 확인 후 분기
**문제**: 매번 SELECT 쿼리 발생
**개선 옵션**:
1. JPA `merge()` 활용 (ID 기반 자동 판단)
2. 도메인 이벤트로 신규/수정 구분
3. `User` 도메인에 `isNew` 플래그 추가

```kotlin
override fun save(user: User): User {
    val entity = userJpaRepository.findByPublicId(user.id.value)
        ?.also { it.updateFromDomain(user) }
        ?: user.toEntity()

    return userJpaRepository.save(entity).toDomain()
}
```

---

### 3-5. `JwtAuthenticationFilter` — early return 체이닝 개선

**현재**: 3개의 `if` 블록에서 각각 `filterChain.doFilter()` 호출 후 `return`
**개선**:

```kotlin
override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    resolveToken(request)
        ?.takeIf { jwtTokenPort.validate(it) && jwtTokenPort.isAccessToken(it) }
        ?.let { token ->
            val auth = UsernamePasswordAuthenticationToken(
                jwtTokenPort.extractUserPublicId(token),
                null,
                listOf(SimpleGrantedAuthority("ROLE_${jwtTokenPort.extractRole(token)}"))
            )
            SecurityContextHolder.getContext().authentication = auth
        }

    filterChain.doFilter(request, response)
}
```

> `filterChain.doFilter()`가 항상 실행되므로 한 번만 호출. Kotlin scope 함수 활용.

---

### 3-6. `JwtTokenPort` — 인터페이스가 너무 비대 (ISP 위반)

**현재**: 8개 메서드가 하나의 인터페이스에 존재
**개선**: 역할별 분리

```kotlin
interface JwtTokenGenerator {
    fun generateAccessToken(userPublicId: String, role: String): String
    fun generateRefreshToken(userPublicId: String, role: String): String
}

interface JwtTokenValidator {
    fun validate(token: String): Boolean
    fun isExpired(token: String): Boolean
    fun isAccessToken(token: String): Boolean
    fun isRefreshToken(token: String): Boolean
}

interface JwtTokenExtractor {
    fun extractUserPublicId(token: String): String
    fun extractRole(token: String): String
}
```

---

### 3-7. `SecurityConfig` — Kotlin DSL 활용

**현재**: Java 스타일 `Customizer` 사용
**개선**: Spring Security Kotlin DSL 활용

```kotlin
@Bean
fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http {
        csrf { disable() }
        sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
        exceptionHandling {
            authenticationEntryPoint = jwtAuthenticationEntryPoint
            accessDeniedHandler = jwtAccessDeniedHandler
        }
        authorizeHttpRequests {
            authorize("/api/auth/register", permitAll)
            authorize("/api/auth/login", permitAll)
            authorize(anyRequest, authenticated)
        }
        addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        cors { }
    }
    return http.build()
}
```

---

### 3-8. `JwtAccessDeniedHandler` / `JwtAuthenticationEntryPoint` — JSON 직접 작성 개선

**현재**: 문자열 템플릿으로 JSON 수동 작성
**개선**: `ObjectMapper`를 주입받아 직렬화하거나, 공통 에러 응답 DTO 활용

---

## 4. 프레젠테이션 레이어

### 4-1. `AuthController` — 하나의 컨트롤러에 모든 인증 엔드포인트

**현재**: Register, Login, GetProfile이 모두 `AuthController`에 위치
**검토**: Profile 조회는 인증과 다른 관심사. 대기업 실무에서는 **User 리소스 기반**으로 분리하는 것이 일반적

```
AuthController   → POST /api/auth/register, POST /api/auth/login
UserController   → GET /api/users/me
```

---

### 4-2. Request DTO — `@field:NotBlank` 사용 권장

**현재**: `@field:NotNull`만 사용
**문제**: `""` (빈 문자열)이 통과됨
**개선**: `@field:NotBlank`로 변경하고, 비밀번호에는 `@field:Size(min = 8)` 등 추가

```kotlin
data class RegisterUserRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 100)
    val password: String,

    @field:NotBlank
    @field:Size(min = 2, max = 50)
    val name: String
)
```

---

### 4-3. `ApiResponse` — `ResponseEntity` 활용 검토

**현재**: 커스텀 `ApiResponse<T>` wrapper 사용
**검토**: 에러 응답은 `ProblemDetail`(RFC 7807), 성공 응답은 `ApiResponse` — 형식이 이원화됨
**개선 옵션**:
1. 성공도 `ProblemDetail` 대신 `ResponseEntity<T>` + `@ResponseStatus`로 통일
2. 또는 에러도 `ApiResponse`로 통일

---

### 4-4. Response DTO의 `companion object` 팩토리 — 확장 함수로 전환

**현재**: `LoginUserResponse.from(result)` 패턴
**개선**: `result.toResponse()` 확장 함수 패턴이 더 코틀린다움

```kotlin
fun LoginUserResult.toResponse() = LoginUserResponse(id, email, role, accessToken)
```

---

## 5. 빌드 & 설정

### 5-1. Java Toolchain 25 + Release 24 — 불필요한 설정

**현재**: `languageVersion = 25`, `release = 24`
**문제**: 순수 Kotlin 프로젝트에서 `JavaCompile` 설정은 혼란을 유발. 실제 컴파일은 `kotlinc`가 담당
**개선**: Kotlin JVM target만 명시

```kotlin
kotlin {
    jvmToolchain(21) // LTS 버전 권장
}
```

> Java 25는 비 LTS. 프로덕션에서는 21(LTS)이 표준.

---

### 5-2. `java` 플러그인 제거

**현재**: `plugins { java; ... }` — 불필요
**개선**: `kotlin("jvm")`이 이미 Java 컴파일 태스크를 포함하므로 `java` 플러그인 제거

---

### 5-3. 테스트 의존성 추가

**현재**: `spring-boot-starter-test`만 존재
**개선**: Kotlin 친화적 테스트 라이브러리 추가

```kotlin
testImplementation("io.mockk:mockk:1.13.13")           // Kotlin-native mocking
testImplementation("io.kotest:kotest-runner-junit5:5.9.1") // BDD 스타일 테스트 (선택)
testImplementation("io.kotest:kotest-assertions-core:5.9.1")
```

---

## 6. 테스트

### 6-1. 테스트 부재 — 가장 큰 개선 포인트

**현재**: `contextLoads()` 하나만 존재
**개선**: 최소한 다음 테스트가 필요

| 구분 | 대상 | 설명 |
|------|------|------|
| 단위 | `User` 도메인 | `register()`, `updateLastLoginAt()`, 유효성 검증 |
| 단위 | `Email`, `UserId` VO | 팩토리 메서드, 검증 로직 |
| 단위 | `RegisterUserService` | MockK으로 Port mock, 중복 이메일 검증 |
| 단위 | `LoginUserService` | 비밀번호 불일치, 비활성 계정 등 |
| 통합 | `UserPersistenceAdapter` | `@DataJpaTest`로 영속성 검증 |
| E2E | `AuthController` | `@SpringBootTest` + `MockMvc`/`WebTestClient` |

---

## 7. 아키텍처 & DDD 관점

### 7-1. 도메인 이벤트 도입 검토

**현재**: 회원가입/로그인 시 side effect가 서비스에 절차적으로 나열
**개선**: `UserRegisteredEvent`, `UserLoggedInEvent` 등 도메인 이벤트 발행

```kotlin
data class UserRegisteredEvent(val userId: UserId, val email: Email)
```

> Spring의 `ApplicationEventPublisher`로 느슨한 결합 달성. 이메일 발송, 감사 로그 등 확장에 유리.

---

### 7-2. sealed class 활용 — 결과 타입

**현재**: 실패 시 예외, 성공 시 Result DTO 반환
**개선 옵션**: Kotlin `sealed class`/`Result`로 명시적 실패 표현

```kotlin
sealed class LoginResult {
    data class Success(val accessToken: String, ...) : LoginResult()
    data class InvalidPassword(val message: String) : LoginResult()
    data class InactiveUser(val message: String) : LoginResult()
}
```

> 예외 기반 흐름 제어 대신 **타입 안전한 분기** 가능. 트레이드오프: 컨트롤러에서 `when` 분기 필요.

---

### 7-3. 패키지 구조 — `infrastructure` 하위 정리

**현재**:
```
infrastructure/
├── auth/          ← JWT + Security Config
├── persistence/   ← 공통 JPA
├── security/      ← CORS, Cookie, 핸들러, PasswordHash
└── user/          ← User 영속성
```

**문제**: `auth`와 `security`의 경계가 모호. `JwtAuthenticationFilter`는 `auth/adapter/in/`에, `JwtAccessDeniedHandler`는 `security/`에 분산
**개선**:

```
infrastructure/
├── config/
│   ├── SecurityConfig.kt
│   ├── JwtProperties.kt
│   ├── CorsProperties.kt
│   └── CookieProperties.kt
├── security/
│   ├── JwtAuthenticationFilter.kt
│   ├── JwtTokenAdapter.kt
│   ├── JwtAccessDeniedHandler.kt
│   ├── JwtAuthenticationEntryPoint.kt
│   └── PasswordHashAdapter.kt
└── user/
    ├── UserPersistenceAdapter.kt
    ├── UserJpaRepository.kt
    ├── UserEntity.kt
    └── UserMapper.kt
```

---

## 8. 요약 — 우선순위별 정리

### 즉시 적용 (코틀린 관용구)
- [ ] `Optional` → Nullable (`?`) 전환
- [ ] `@JvmStatic`, `@JvmOverloads` 제거
- [ ] `UserEntity`에서 `lateinit` + 보조 생성자 제거
- [ ] `UserMapper` → 확장 함수로 전환
- [ ] `@field:NotBlank` 적용
- [ ] `JwtAuthenticationFilter` scope 함수 활용
- [ ] `java` 플러그인 제거, JVM toolchain 정리

### 구조 개선 (Hexagonal/Clean)
- [ ] `UserRepository`를 Out Port로 이동 (역할별 분리)
- [ ] `JwtTokenPort` 인터페이스 분리 (ISP)
- [ ] `infrastructure` 패키지 구조 정리
- [ ] 컨트롤러 분리 (Auth vs User)

### 심화 (DDD/실무)
- [ ] 도메인 이벤트 도입
- [ ] `sealed class` 결과 타입 검토
- [ ] 테스트 코드 작성
- [ ] 코틀린 테스트 라이브러리 추가 (MockK, Kotest)

### 선택 (팀 컨벤션)
- [ ] Security Kotlin DSL 전환
- [ ] 응답 형식 통일 (ApiResponse vs ProblemDetail)
- [ ] Command DTO에 VO 적용 여부
