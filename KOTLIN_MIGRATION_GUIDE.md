# Java -> Kotlin 마이그레이션 (완전 초보용)

이 문서는 이 프로젝트에서 그대로 복붙해서 따라할 수 있게 작성했습니다.

## 핵심 원칙 3개

- 한 번에 많이 바꾸지 않는다. (파일 1~2개씩)
- 매 단계마다 `./gradlew test`로 확인한다.
- 실패하면 바로 최근 커밋으로 돌아간다.

## 먼저 꼭 알아야 할 혼용 규칙

Java와 Kotlin을 섞는 기간에는 아래 때문에 자주 깨집니다.

1. Java `record`의 접근자: `email()`  
Kotlin `data class`의 접근자: Java에서 `getEmail()`

2. Java static 메서드  
Kotlin에서는 `companion object`로 바뀜  
Java에서 기존처럼 `User.register(...)`로 호출하려면 `@JvmStatic` 필요

이 문서는 이 문제를 피하는 순서로 진행합니다.

## 0단계: 기준선 만들기

```bash
./gradlew test
git add .
git commit -m "chore: baseline before kotlin migration"
```

## 1단계: Kotlin 빌드 설정 확인

`build.gradle`의 플러그인 ID는 반드시 아래처럼 `org.jetbrains...` 이어야 합니다.

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.2'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'org.jetbrains.kotlin.jvm' version '2.2.0'
    id 'org.jetbrains.kotlin.plugin.spring' version '2.2.0'
    id 'org.jetbrains.kotlin.plugin.jpa' version '2.2.0'
}
```

추가 의존성:

```groovy
dependencies {
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
}
```

검증:

```bash
./gradlew test
```

## 2단계: enum 2개를 Kotlin으로 변환 (첫 실습)

### 2-1. Kotlin 파일 생성

```bash
mkdir -p src/main/kotlin/com/dochiri/hexagonal/domain/user
```

파일: `src/main/kotlin/com/dochiri/hexagonal/domain/user/UserRole.kt`

```kotlin
package com.dochiri.hexagonal.domain.user

enum class UserRole {
    USER, ADMIN
}
```

파일: `src/main/kotlin/com/dochiri/hexagonal/domain/user/UserStatus.kt`

```kotlin
package com.dochiri.hexagonal.domain.user

enum class UserStatus {
    ACTIVE, INACTIVE, DELETED
}
```

### 2-2. Java 파일 삭제

```bash
rm src/main/java/com/dochiri/hexagonal/domain/user/UserRole.java
rm src/main/java/com/dochiri/hexagonal/domain/user/UserStatus.java
```

### 2-3. 검증

```bash
./gradlew test
git add .
git commit -m "refactor(kotlin): migrate user enums"
```

## 3단계: Exception 클래스 4개 변환

먼저 쉬운 클래스부터 감 잡기 좋습니다.

폴더 생성:

```bash
mkdir -p src/main/kotlin/com/dochiri/hexagonal/domain/user/exception
```

파일: `src/main/kotlin/com/dochiri/hexagonal/domain/user/exception/DuplicateEmailException.kt`

```kotlin
package com.dochiri.hexagonal.domain.user.exception

class DuplicateEmailException(email: String) : RuntimeException("이미 가입된 이메일입니다: $email")
```

파일: `src/main/kotlin/com/dochiri/hexagonal/domain/user/exception/InactiveUserException.kt`

```kotlin
package com.dochiri.hexagonal.domain.user.exception

class InactiveUserException(message: String) : RuntimeException(message)
```

파일: `src/main/kotlin/com/dochiri/hexagonal/domain/user/exception/InvalidPasswordException.kt`

```kotlin
package com.dochiri.hexagonal.domain.user.exception

class InvalidPasswordException(message: String) : RuntimeException(message)
```

파일: `src/main/kotlin/com/dochiri/hexagonal/domain/user/exception/UserNotFoundException.kt`

```kotlin
package com.dochiri.hexagonal.domain.user.exception

class UserNotFoundException(message: String) : RuntimeException(message)
```

Java 파일 삭제:

```bash
rm src/main/java/com/dochiri/hexagonal/domain/user/exception/DuplicateEmailException.java
rm src/main/java/com/dochiri/hexagonal/domain/user/exception/InactiveUserException.java
rm src/main/java/com/dochiri/hexagonal/domain/user/exception/InvalidPasswordException.java
rm src/main/java/com/dochiri/hexagonal/domain/user/exception/UserNotFoundException.java
```

검증:

```bash
./gradlew test
git add .
git commit -m "refactor(kotlin): migrate user exceptions"
```

## 4단계: Port 인터페이스 변환

이 단계는 Java/Kotlin 혼용에서 안전합니다.

폴더:

```bash
mkdir -p src/main/kotlin/com/dochiri/hexagonal/application/user/port/in
mkdir -p src/main/kotlin/com/dochiri/hexagonal/application/user/port/out
```

### 4-1. in 포트

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/in/GetUserProfileQuery.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.`in`

import com.dochiri.hexagonal.application.user.dto.UserProfileResult

interface GetUserProfileQuery {
    fun getProfile(userPublicId: String): UserProfileResult
}
```

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/in/LoginUserUseCase.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.`in`

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand
import com.dochiri.hexagonal.application.user.dto.LoginUserResult

interface LoginUserUseCase {
    fun login(loginUserCommand: LoginUserCommand): LoginUserResult
}
```

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/in/RegisterUserUseCase.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.`in`

import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand
import com.dochiri.hexagonal.application.user.dto.RegisterUserResult

interface RegisterUserUseCase {
    fun register(registerUserCommand: RegisterUserCommand): RegisterUserResult
}
```

### 4-2. out 포트

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/out/JwtTokenPort.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.out

interface JwtTokenPort {
    fun generateAccessToken(userPublicId: String, role: String): String
    fun generateRefreshToken(userPublicId: String, role: String): String
    fun validate(token: String): Boolean
    fun extractUserPublicId(token: String): String
    fun extractRole(token: String): String
    fun isExpired(token: String): Boolean
    fun isAccessToken(token: String): Boolean
    fun isRefreshToken(token: String): Boolean
}
```

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/out/LoadUserPort.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.out

import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId
import java.util.Optional

interface LoadUserPort {
    fun loadById(userId: UserId): Optional<User>
    fun loadByEmail(email: Email): Optional<User>
}
```

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/out/PasswordHashPort.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.out

interface PasswordHashPort {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
```

파일: `src/main/kotlin/com/dochiri/hexagonal/application/user/port/out/SaveUserPort.kt`

```kotlin
package com.dochiri.hexagonal.application.user.port.out

import com.dochiri.hexagonal.domain.user.User

interface SaveUserPort {
    fun save(user: User): User
}
```

Java 포트 삭제:

```bash
rm src/main/java/com/dochiri/hexagonal/application/user/port/in/GetUserProfileQuery.java
rm src/main/java/com/dochiri/hexagonal/application/user/port/in/LoginUserUseCase.java
rm src/main/java/com/dochiri/hexagonal/application/user/port/in/RegisterUserUseCase.java
rm src/main/java/com/dochiri/hexagonal/application/user/port/out/JwtTokenPort.java
rm src/main/java/com/dochiri/hexagonal/application/user/port/out/LoadUserPort.java
rm src/main/java/com/dochiri/hexagonal/application/user/port/out/PasswordHashPort.java
rm src/main/java/com/dochiri/hexagonal/application/user/port/out/SaveUserPort.java
```

검증:

```bash
./gradlew test
git add .
git commit -m "refactor(kotlin): migrate user ports"
```

## 5단계: DTO는 서비스와 같이 변환

주의: DTO(Java record)만 먼저 Kotlin으로 바꾸면 Java 서비스에서 `email()` 호출이 깨질 수 있습니다.

- 안전한 방법: DTO + 서비스를 같은 커밋에서 같이 변환
- 이 단계에서 변환 대상:
`application/user/dto/*.java`
`application/user/service/**/*.java`

검증:

```bash
./gradlew test
```

## 6단계: `User.java` 변환 (중요)

`User.register(...)`, `User.of(...)`를 Java 코드에서도 바로 쓰려면 `@JvmStatic`이 필요합니다.

예시:

```kotlin
companion object {
    @JvmStatic
    fun register(...) = ...

    @JvmStatic
    fun of(...) = ...
}
```

검증:

```bash
./gradlew test
```

## 7단계: `Email`, `UserId` 변환

이 파일들은 Java 코드에서 `email.value()`처럼 record 메서드 스타일을 쓰고 있어서 주의가 필요합니다.

- 가장 쉬운 방법: 관련 Java 호출부까지 Kotlin으로 같이 바꿀 때 진행
- 초보자 권장: 이 단계는 DTO/서비스를 Kotlin으로 옮긴 뒤 진행

검증:

```bash
./gradlew test
```

## 8단계: 인프라/컨트롤러 변환

대상:

- `src/main/java/com/dochiri/hexagonal/infrastructure/**`
- `src/main/java/com/dochiri/hexagonal/presentation/**`

검증:

```bash
./gradlew test
```

## 9단계: 마무리 점검

남은 Java 파일 확인:

```bash
find src -name "*.java"
```

최종 테스트:

```bash
./gradlew clean test
```

## 지금 바로 할 일 (딱 1회차)

오늘은 아래만 수행:

1. 2단계(enum 2개) 수행
2. `./gradlew test` 통과
3. 커밋

여기까지 성공하면 다음 회차에서 3단계(exception 4개) 진행.
