## `admin` 테이블 사용 로직 정리 보고서

### 목표

도메인 정리를 시작하기 위해, **`admin` 테이블을 직접 사용하는 모든 로직**(API/서비스/리포지토리/테스트/스키마)을 한 곳에 모아 “어디서 무엇을 위해 쓰는지”를 정리합니다.

---

## 1) DB 스키마 관점

Flyway 초기 스키마(`V1__init.sql`)에 `admin` 테이블이 존재합니다.

```15:23:src/main/resources/db/migration/V1__init.sql
CREATE TABLE admin (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(255) NOT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 컬럼 요약

- **PK**: `id`
- **로그인 식별자**: `username` (unique)
- **비밀번호(해시)**: `password`
- **메타**: `name`, `phone_number`

---

## 2) 엔티티/리포지토리/서비스 (직접 사용)

### 2.1 엔티티: `AdminEntity` → 테이블 `admin`

```6:36:src/main/java/com/_1000meal/adminlogin/entity/AdminEntity.java
@Entity
@Table(name = "admin")
public class AdminEntity {
  private Long id;
  private String username;
  private String password;
  private String name;
  private String phoneNumber;
  public void changePassword(String encodedPassword) { ... }
}
```

### 2.2 리포지토리: `AdminRepository`

```9:11:src/main/java/com/_1000meal/adminlogin/repository/AdminRepository.java
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByUsername(String username);
}
```

### 2.3 서비스: `AdminService` (admin 테이블 CRUD)

```21:78:src/main/java/com/_1000meal/adminlogin/service/AdminService.java
public class AdminService {
  public AdminEntity signup(AdminSignupRequest req) { ... adminRepository.save(admin); }
  public AdminEntity authenticate(String username, String rawPassword) { ... }
  public AdminEntity getAdminByUsername(String username) { ... }
  public void changePassword(String username, PasswordChangeRequest request) { ... adminRepository.save(admin); }
}
```

#### 이 서비스가 하는 일

- **관리자 가입**: `admin` 테이블에 row 생성
- **관리자 인증(비밀번호 확인)**: `admin.username`으로 조회 후 `admin.password` 비교
- **관리자 비밀번호 변경**: `admin.password` 갱신

---

## 3) API 레벨 사용처 (Controller)

### 3.1 `AdminAuthController`가 `admin` 테이블을 직접 쓰는 API

`AdminAuthController`는 **두 계열**을 함께 가지고 있습니다.

1) **`/api/signup`** → `AdminService.signup()` 호출 → **`admin` 테이블 사용(직접)**

```61:68:src/main/java/com/_1000meal/adminlogin/controller/AdminAuthController.java
@PostMapping("/signup")
public ApiResponse<String> signup(@RequestBody AdminSignupRequest request) {
    adminService.signup(request);
    return ApiResponse.success("관리자 회원가입 완료", SuccessCode.CREATED);
}
```

2) 반면 아래 API들은 `AdminAccountAuthService`를 호출하며, 이는 `accounts/admin_profiles` 기반입니다(= `admin` 테이블과 무관).
- `POST /api/login`
- `GET /api/me`
- `PATCH /api/password`

즉, **관리자 API가 “가입은 admin 테이블”, “로그인/내정보/비번변경은 accounts/admin_profiles”로 분산**되어 있습니다.

---

## 4) 테스트 코드에서의 사용

`AdminServiceTest`는 `AdminRepository`를 mocking하여 `AdminService`의 로직을 검증합니다.  
즉, 테스트 역시 `admin` 테이블 라인을 전제로 존재합니다.

- 파일: `src/test/java/com/_1000meal/adminlogin/service/AdminServiceTest.java`

---

## 5) 보안/라우팅 관점에서의 관찰(정리 착수에 중요한 포인트)

### 5.1 SecurityConfig 화이트리스트/권한

`SecurityConfig`의 AUTH_WHITELIST에는 `/api/*`가 포함되어 있지 않습니다.  
즉, `/api/login`, `/api/signup`, `/api/me`, `/api/password`는 기본 정책상 “인증 필요”로 떨어질 가능성이 큽니다(구현/필터 동작에 따라).

```30:44:src/main/java/com/_1000meal/global/config/SecurityConfig.java
private static final String[] AUTH_WHITELIST = {
  "/api/v1/auth/signup",
  "/api/v1/auth/login",
  ...
};
```

> 이 보고서는 “admin 테이블 사용 로직”에 집중하므로, 보안 정책 변경안은 별도 작업으로 분리하는 것을 권장합니다.

---

## 6) 결론: `admin` 테이블이 사용되는 “실제 로직” 목록

### 직접 사용(= admin 테이블 CRUD)

- **DB**
  - `src/main/resources/db/migration/V1__init.sql` → `CREATE TABLE admin`
- **Entity/Repository**
  - `AdminEntity` (`@Table(name="admin")`)
  - `AdminRepository.findByUsername(...)`
- **Service**
  - `AdminService.signup/authenticate/getAdminByUsername/changePassword`
- **API**
  - `POST /api/signup` (관리자 회원가입) — `AdminService.signup` 경유
- **Test**
  - `AdminServiceTest`

### 간접적으로는 “관리자 기능”이지만 admin 테이블은 안 쓰는 로직

- `AdminAuthController`의 `/api/login`, `/api/me`, `/api/password`
  - `AdminAccountAuthService` 사용
  - 실제 저장소: `accounts` + `admin_profiles`

---

## 7) 정리 시작 시 권장 체크리스트(다음 단계)

`admin` 정리를 시작한다면, 바로 다음 결정이 필요합니다.

- **결정 A**: 관리자 계정의 정본을 `accounts/admin_profiles`로 통일할지?
  - 통일한다면: `admin` 테이블 및 `AdminService(/api/signup)` 라인을 단계적으로 제거(마이그레이션 포함)
- **결정 B**: `/api/*` 관리자 API를 `/api/v1/admin/*` 또는 `/api/v1/auth/*`로 재배치/통합할지?

원하시면, 다음 단계로 “`admin` → `accounts/admin_profiles` 마이그레이션 설계(데이터 이행 SQL, 롤/상태 세팅, 중복 처리)” 보고서까지 이어서 작성할 수 있습니다.

