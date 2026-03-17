## `API_ENDPOINTS_ONLY_IN_DOMAIN.md` 대체/정리 보고서

### 요약

`API_ENDPOINTS_ONLY_IN_DOMAIN.md`에 있던 15개 API를 기준으로, **(1) 동일/유사 기능의 대체 API 존재 여부**, **(2) 관련 테이블·필드(엔티티 기준)**, **(3) 정리(Deprecated/삭제) 권장 사항**을 정리했습니다.

> 표기 규칙
> - **대체됨**: 앱(`FRONTEND_API_USAGE.md`)에서 호출하는 다른 엔드포인트가 있고 기능이 사실상 동일/상위호환
> - **미사용/미대체**: 앱에서 호출 흔적이 없고, 동일 기능의 대체 엔드포인트도 명확하지 않음(백오피스/운영용일 수 있음)

---

## 1) Default Menu (기본(핀) 메뉴)

### 1.1 `GET /api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus`
- **대체 여부**: 미사용/미대체 (앱 사용 목록에 없음)
- **관련 테이블/필드**
  - `default_group_menu`
    - `id`, `store_id`, `menu_group_id`, `menu_name`, `active`, `start_date`, `end_date`, `created_by_account_id`, `created_at`, `updated_at`
    - 근거: `DefaultGroupMenu` `@Table(name="default_group_menu")`
  - (조회 결과에 포함될 수 있는 전개 대상)
    - `menu_group` (연결 FK: `menu_group_id`)
- **비고(로직)**: `DefaultGroupMenuService.getDefaultMenus(...)`가 사용
- **정리 권장**
  - 실제로 앱/관리자 UI에서 “핀 메뉴” 기능을 쓰지 않는다면: **엔드포인트를 Deprecated**로 표시하고, 일정 기간 이후 제거 후보.
  - 반대로 운영에서 필요하다면: `FRONTEND_API_USAGE.md`에 추가(명시)해 **사용처를 문서화**하는 게 좋음.

### 1.2 `POST /api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus`
- **대체 여부**: 미사용/미대체
- **관련 테이블/필드**
  - `default_group_menu` (삽입/갱신)
  - `group_daily_menu` / `group_daily_menu_item` (조건부 생성/갱신)
    - 근거: `DefaultGroupMenuService.pinDefaultMenu(...)`에서 `materializeIfAbsentForDate(...)` 호출
    - `GroupDailyMenu`:
      - `group_daily_menu(id, menu_group_id, date)`
      - `group_daily_menu_item(group_daily_menu_id, name)` (메뉴명 리스트)
- **정리 권장**
  - “핀 메뉴” 기능이 정책적으로 폐기라면:
    - **테이블 단위 정리 후보**: `default_group_menu`, `group_daily_menu(_item)` 중 `default_group_menu`에 의해 생성되는 항목(전개 로직 포함)
    - 단, `group_daily_menu`는 일반 “그룹 일간 메뉴”에도 쓰이므로 **전면 삭제는 금지**. “default 메뉴 전개 로직”만 걷어내는 방향으로.

### 1.3 `DELETE /api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus`
- **대체 여부**: 미사용/미대체
- **관련 테이블/필드**
  - `default_group_menu.end_date`, `default_group_menu.active` (비활성/종료 처리)
  - `group_daily_menu(_item)` (오늘 기준으로 전개 갱신)
- **정리 권장**: 1.2와 동일

### 1.4 `PATCH /api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus/{defaultMenuId}/activate`
- **대체 여부**: 미사용/미대체
- **관련 테이블/필드**
  - `default_group_menu`
    - `active`, `start_date`, `end_date` 갱신
  - `group_daily_menu` / `group_daily_menu_item`
    - 오늘 날짜의 그룹 메뉴 전개(없으면 생성)
- **정리 권장**: 1.2와 동일

---

## 2) QR Store Query (QR 매장 조회)

### 2.1 `GET /api/v1/qr/stores`
- **대체 여부**: 부분 대체됨
  - 앱은 `GET /api/v1/qr/stores/{qrToken}`만 사용(단건 조회).
  - “전체 목록 조회” 자체는 단건 API로 대체되지 않음(운영/관리용 성격).
- **관련 테이블/필드**
  - `store_qr`
    - `id`, `store_id`, `qr_token`, `menu_group_id`, `is_active`, `created_at`
    - 근거: `StoreQr` `@Table(name="store_qr")`
  - `store` (조인: `store_id`)
  - `menu_group` (선택적으로 `menu_group_id`를 이름으로 치환)
- **정리 권장**
  - 운영에서 “QR 전체 목록” 화면이 없다면: 엔드포인트를 **ADMIN 권한 제한**(코드에도 TODO 있음) + 문서상 “운영자 전용”으로 명시.
  - 아예 불필요하면: `/api/v1/qr/stores/{qrToken}`만 남기고 **목록 API는 Deprecated→삭제**.

---

## 3) Store Image URL

### 3.1 `POST /api/v1/stores/image-url`
- **대체 여부**: 미사용/미대체 (앱 호출 목록에 없음)
- **관련 테이블/필드**
  - `store`
    - `image_url` (엔티티 필드: `Store.imageUrl`)
    - 근거: `StoreController.setImageUrl(...)` → `StoreService.setImageUrl(...)` → `Store.updateImageUrl(...)`
- **정리 권장**
  - 앱/관리자에서 사용 계획이 없으면: 엔드포인트 Deprecated 후보.
  - 이미지 업로드 정책(S3 presign 등)과 결합해 “매장 이미지 변경” 기능이 필요하다면:
    - 별도 “관리자 전용” 라우트로 묶고 인증/권한(ADMIN+store scope)을 명확히 하는 게 좋음.

---

## 4) Auth / Account (사용자 계정) — 앱 미사용 API

### 4.1 `POST /api/v1/auth/logout`
- **대체 여부**: 미사용/미대체 (앱 목록에 “로그아웃” 자체가 없음)
- **관련 테이블/필드**
  - `refresh_token`
    - `token_hash`, `revoked_at`, `last_used_at` 등
    - 근거: `AuthController.logout` → `RefreshTokenService.logout` → `token.revoke(now)`
- **정리 권장**
  - 프론트에서 로그아웃 UX가 필요하면, **앱 문서에 추가**하고 사용처를 명확히.
  - 서버 측으로는 안전한 편(토큰 revoke)이라 제거보단 “사용처 정리”가 우선.

### 4.2 `POST /api/v1/auth/change-password`
- **대체 여부**: 미사용/미대체
- **관련 테이블/필드**
  - `accounts.password_hash`
    - 근거: `AccountController.changePassword` → `AccountService.changePasswordByAccountId(...)` (계정 비밀번호 갱신)
- **정리 권장**
  - 앱에 “비밀번호 변경” 기능이 없다면: 유지해도 되지만, 장기적으로는 **실사용 여부에 따라 Deprecated** 검토.

### 4.3 `POST /api/v1/auth/email/change/request`
### 4.4 `POST /api/v1/auth/email/change/confirm`
- **대체 여부**: **대체됨(레거시)**
  - 앱은 `/api/v1/account/email/start` → `/code` → `/verify` **새 플로우**를 사용
  - 기존 `/api/v1/auth/email/change/*`는 “로그인 후 이메일 변경” 레거시 절차로 보임
- **관련 테이블/필드**
  - `email_verification_tokens`
    - `email`, `code`, `verified`, `expires_at`, `created_at`
    - 근거: `AccountService.requestChangeEmail/confirmChangeEmail`에서 `emailService.issueAndStoreCode/verifyCode/consumeAllFor`
  - `accounts.email`
    - 근거: `Account.changeEmail(newEmail)`
- **정리 권장**
  - 레거시 `/api/v1/auth/email/change/*`는 **Deprecated** 처리하고, 문서/프론트에서 새 플로우(`/api/v1/account/email/*`)로 통일 권장.
  - 데이터 레벨 정리는 “테이블 삭제”가 아니라 “엔드포인트/DTO 정리”가 적절:
    - `email_verification_tokens`는 새 플로우에서도 그대로 필요.

---

## 5) Admin Auth (별도 관리자 API `/api/*`)

> 이 영역은 **앱(Flutter) 기준으로는 사용하지 않는 것으로 보이며**, 내부적으로도 “관리자 계정” 저장소가 `admin` 테이블과 `accounts/admin_profiles`가 혼재되어 있어 정리가 필요합니다.

### 5.1 `POST /api/login`
- **대체 여부**: **기능상 대체 가능(권장: `/api/v1/auth/login`)**
  - 현재 `/api/login`은 `AdminAccountAuthService.login(...)`을 타며 `accounts` 기반 ADMIN 로그인을 수행합니다.
  - 앱은 `/api/v1/auth/login`을 사용(통합 로그인).
- **관련 테이블/필드**
  - `accounts`: `user_id`, `password_hash`, `role`, `status`, `email`
  - `admin_profiles`: `account_id`, `store_id`, `display_name`, `admin_level`
- **정리 권장**
  - “관리자도 통합 Auth로 로그인”이 목표면 `/api/login`은 Deprecated 후보.
  - 단, `/api/login` 응답 스펙이 앱/웹 콘솔에 맞춰 다르면 교체 시 프론트 영향 확인 필요.

### 5.2 `GET /api/me`
- **대체 여부**: 기능상 대체 가능
  - 사용자용은 `/api/v1/auth/me` (앱 사용)
  - 관리자 전용 “내 정보”는 `/api/v1/auth/me`로도 커버 가능하게 설계하는 편이 단순
- **관련 테이블/필드**
  - `accounts`, `admin_profiles`
- **정리 권장**: `/api/*` 계열을 `/api/v1/auth/*`로 통합 권장

### 5.3 `PATCH /api/password`
- **대체 여부**: 기능상 대체 가능
  - 사용자 비번 변경: `/api/v1/auth/change-password`
  - 관리자 비번 변경도 통합 endpoint로 묶는 편이 단순(권한/role로 분기)
- **관련 테이블/필드**
  - `accounts.password_hash` (실제 변경은 `Account.changePassword(...)`)
- **정리 권장**
  - 관리자/사용자 비번 변경 엔드포인트를 역할 기반으로 통합하거나, 최소한 `/api/*`를 Deprecated.

### 5.4 `POST /api/signup`
- **대체 여부**: **대체 관계가 가장 불명확(주의)**
  - `/api/signup`은 `AdminService.signup(...)`을 타고 **`admin` 테이블**에 저장합니다.
  - 반면 `/api/login`, `/api/me`, `/api/password`는 `accounts/admin_profiles` 기반입니다.
  - 즉, `/api/signup`으로 만든 계정은 `/api/login`과 **저장소가 달라** 로그인되지 않을 수 있는 구조입니다.
- **관련 테이블/필드**
  - `admin`: `username`, `password`, `name`, `phone_number`
    - 근거: `AdminEntity` `@Table(name="admin")`
- **정리 권장(강력)**
  - 관리자 계정 저장소를 **단일화**해야 합니다.
    - (권장) `admin` 테이블 기반 흐름을 폐기하고 `accounts + admin_profiles`로 통일, 또는 그 반대
  - 통일 후 `admin` 테이블 및 관련 코드(`AdminService`, `AdminRepository`, `AdminEntity`)는 정리 후보.

---

## 결론: “대체됨/정리 우선순위” 추천

1. **레거시 이메일 변경 API 정리(대체됨 확실)**  
   - `/api/v1/auth/email/change/request`, `/api/v1/auth/email/change/confirm`  
   → `/api/v1/account/email/*`로 통일

2. **관리자 `/api/*` 라인 정리(혼재 구조 해소 필요)**  
   - `/api/signup`이 `admin` 테이블을 쓰는 점이 특히 위험(로그인 라인과 저장소 불일치)

3. **Default Menu(핀 메뉴) 기능은 사용 여부 결정 후 정리**  
   - 사용 안 하면 엔드포인트 Deprecated + 관련 “전개 로직” 정리(단, `group_daily_menu` 자체는 유지)

4. **QR 목록(`/api/v1/qr/stores`)은 운영/관리 기능 여부 확인 후 권한/정리**  

