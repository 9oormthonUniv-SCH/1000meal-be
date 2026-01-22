# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build
./gradlew clean build              # Full build with tests
./gradlew clean build -x test      # Build without tests

# Run
./gradlew bootRun                  # Run application locally (port 8080)

# Test
./gradlew test                           # Run all tests
./gradlew test --tests AuthServiceTest   # Run specific test class
./gradlew test --tests "*.AuthServiceTest.testMethod"  # Run specific test method
```

## Architecture Overview

Spring Boot 3.2.2 REST API with Java 17. Layered architecture with domain-driven module separation.

### Module Structure

```
src/main/java/com/_1000meal/
├── auth/          # User authentication, signup, login, password reset, JWT
├── store/         # Store CRUD and viewing
├── menu/          # Daily/weekly menu management
├── favorite/      # Store favorites
├── notice/        # Notice board
├── fcm/           # Firebase Cloud Messaging
├── email/         # Email service, verification tokens
├── adminlogin/    # Admin authentication
└── global/        # Cross-cutting concerns
    ├── config/    # Spring configs (Security, Swagger, Cache, CORS)
    ├── error/     # Exception handling, error codes
    ├── response/  # Unified API response wrapper
    ├── security/  # JWT filter, authentication provider
    └── constant/  # Roles enum
```

Each module follows: `controller/` → `service/` → `domain/` (or `entity/`) → `repository/` → `dto/`

### Key Patterns

**API Response Format**: All endpoints return `ApiResponse<T>`:
```java
ApiResponse.ok(data)                        // 200 OK
ApiResponse.success(data, SuccessCode.XXX)  // Other 2xx
ApiResponse.error(data, ErrorCodeIfs)       // Error response
```

**API Versioning**: All endpoints use `/api/v1/` prefix

**Authentication**: JWT-based with `@AuthenticationPrincipal AuthPrincipal` injection

**Error Handling**: Throw `CustomException(ErrorCodeIfs)` - handled by `GlobalExceptionHandler`

## Tech Stack

- **Database**: MySQL with JPA/Hibernate (ddl-auto: update)
- **Security**: JWT (JJWT 0.11.5) + OAuth2 (Google, Naver, Kakao)
- **Cache**: Caffeine (3-second TTL for store lists)
- **Storage**: AWS S3
- **Notifications**: Firebase Cloud Messaging
- **Docs**: Springdoc OpenAPI (Swagger at `/swagger-ui.html`)
- **Monitoring**: Actuator + Prometheus (`/actuator/prometheus`)

## Testing

Unit tests use JUnit 5 + Mockito with `@ExtendWith(MockitoExtension.class)`. Tests are in `src/test/java/com/_1000meal/{module}/service/`.

Pattern:
```java
@ExtendWith(MockitoExtension.class)
class SomeServiceTest {
    @Mock Repository repo;
    @InjectMocks SomeService service;

    @Test @DisplayName("설명")
    void testMethod() { ... }
}
```

## Environment Variables

Required for runtime:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`
- `MAIL_USERNAME`, `MAIL_PASSWORD`
- `ACCESS_KEY`, `SECRET_KEY` (AWS)
- `FCM_SERVICE_ACCOUNT_BASE64` (optional)

## Deployment

Docker-based deployment via GitHub Actions:
- Push to `develop` → deploys to test environment
- Push to `main` → deploys to production

Docker build uses `eclipse-temurin:17-jre-jammy` base image.
