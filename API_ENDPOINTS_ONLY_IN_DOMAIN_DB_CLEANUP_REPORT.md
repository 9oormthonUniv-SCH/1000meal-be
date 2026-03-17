## `API_ENDPOINTS_ONLY_IN_DOMAIN` 연관 도메인 기반 DB 정리/수정 보고서

이 문서는 `API_ENDPOINTS_ONLY_IN_DOMAIN.md`에 있는 API들을 “타고 들어가며(Controller → Service → Entity/Repository)” 확인했을 때 드러난 **DB/엔티티 레벨 정리(삭제/통합/수정) 후보**를 정리합니다.

> 범위
> - “앱에서 안 쓰는 API”라는 사실만으로 테이블을 삭제하지는 않습니다.
> - 다만 **중복 저장소**, **이중 정본(source of truth)**, **실사용 대비 유지 비용이 큰 필드/테이블**은 정리 후보로 분류합니다.

---

## 1) 관리자 계정 저장소 중복: `admin` 테이블 vs `accounts/admin_profiles`

### 관측

- `/api/signup`은 `AdminService.signup()`을 통해 **`admin` 테이블**(`AdminEntity`)에 저장합니다.
  - `AdminEntity` → `@Table(name = "admin")`
  - 필드: `username`, `password`, `name`, `phoneNumber`
- 그런데 `/api/login`, `/api/me`, `/api/password`는 `AdminAccountAuthService`를 통해 **`accounts` + `admin_profiles`**를 사용합니다.
  - `AdminAccountAuthService.login()`은 `AccountRepository.findByUserIdAndStatus(...)`로 `accounts`를 조회
  - 관리자 소속/표시명은 `admin_profiles`에서 조회

즉, **관리자 가입과 관리자 로그인(및 프로필/권한)의 저장소가 분리**되어 있습니다.  
이 구조는 “가입한 관리자가 로그인 불가” 같은 운영 장애로 이어질 수 있고, DB 관점에서도 **중복 정본**입니다.

### 관련 테이블/필드(근거)

- `admin`
  - `username`, `password`, `name`, `phone_number`
- `accounts`
  - `user_id`, `password_hash`, `role`, `status`, `email`
- `admin_profiles`
  - `account_id`, `store_id`, `display_name`, `admin_level`

### 정리 권장(강력)

1) **관리자 저장소를 단일화** (둘 중 하나를 정본으로 선택)
- 권장안: 이미 운영 로직이 `accounts/admin_profiles`로 돌아가므로, `admin` 테이블은 폐기(마이그레이션 후 제거) 방향이 안전합니다.

2) 단일화 후 DB 정리 후보
- `admin` 테이블 제거(+ `AdminEntity`, `AdminRepository`, `AdminService.signup/authenticate/changePassword` 정리)
- 또는 반대로 `accounts/admin_profiles`를 제거하려면 보안/인증 전체 구조를 다시 짜야 해서 비용이 큼

---

## 2) “남은 수량” 정본 중복: `store.remain` vs `menu_group_stock.stock`

### 관측

- 재고 차감(QR, 관리자 차감 등)은 **`menu_group_stock.stock`**을 정본으로 사용합니다.
- 그런데 `Store` 엔티티에는 `remain`(남은 수량) 필드가 존재하고,
  - FCM 재고 마감 알림 후보 조회에서 `mgs.stock`이 없으면 `s.remain`을 fallback으로 사용합니다.

```59:78:src/main/java/com/_1000meal/favorite/repository/FavoriteStoreRepository.java
select new ... StockDeadlineCandidate(..., mgs.stock, s.remain)
...
left join mg.stock mgs
```

```57:58:src/main/java/com/_1000meal/fcm/service/StockDeadlineNotificationService.java
int remain = target.groupStock() != null ? target.groupStock() : safeRemain(target.storeRemain());
```

즉, DB 관점에서 “남은 수량”이 **두 군데에 존재**합니다.

### 관련 테이블/필드(근거)

- `menu_group_stock`
  - `stock`(정본), `capacity`, `menu_group_id`
- `store`
  - `remain`(fallback/레거시로 보임)

### 정리 권장

1) 정책 결론: “재고는 그룹 재고가 정본”이면
- `store.remain`은 **Deprecated/삭제 후보**
- FCM 후보 조회도 `store.remain` fallback을 제거하고 `menu_group_stock.stock` 기반으로만 운영(또는 “기본 그룹의 stock”으로 강제)

2) 만약 `store.remain`이 반드시 필요하다면(그룹이 없는 매장/레거시)
- `store.remain`을 업데이트하는 “정본 갱신 로직”이 있어야 하는데, 현재는 그 흐름이 약해 보입니다.
- 이 경우 오히려 `menu_group_stock`과의 동기화 전략을 명시해야 합니다.

---

## 3) `DailyMenu.stock` 필드의 실사용/정본성 저하

### 관측

- `DailyMenu`에 `stock`이 있고 리포지토리에도 `findStockByStoreIdAndDate(...)`가 존재합니다.
- 하지만 메뉴/재고 노출은 대부분 `MenuGroupStock` 합계(`totalStock`)를 기반으로 하고, `DailyMenu.stock`은 사용처가 제한적/레거시로 보입니다.

```22:31:src/main/java/com/_1000meal/menu/repository/DailyMenuRepository.java
select dm.stock ... Optional<Integer> findStockByStoreIdAndDate(...)
```

또한 `DailyMenu`에 `deductStock()` 같은 메서드가 있으나, QR 차감은 `menu_group_stock`에서 수행됩니다.

### 관련 테이블/필드(근거)

- `daily_menu` (엔티티 `DailyMenu`)
  - `stock`, `is_open`, `is_holiday`, `date`, `weekly_menu_id`, `store_id`
- `menu_group_stock`
  - `stock`(실사용 정본)

### 정리 권장

- “재고 정본은 그룹 재고”로 확정이면:
  - `daily_menu.stock`은 Deprecated/삭제 후보
  - `DailyMenuRepository.findStockByStoreIdAndDate`는 삭제 후보
  - `DailyMenu.deductStock/updateStock`는 삭제 후보
- 다만 `DailyMenu.isOpen/isHoliday`는 실제로 운영 상태 판단에 쓰이고 있어 유지 가치가 있습니다.

---

## 4) Default(핀) 메뉴 규칙 테이블의 유지 비용 점검

### 관측

`default_group_menu`는 “기본(핀) 메뉴 규칙”을 저장하고, 필요 시 `group_daily_menu`를 생성/갱신(전개)합니다.

- `default_group_menu`는 규칙성 데이터(기간/활성)를 갖고,
- 실제 일별 메뉴는 `group_daily_menu` + `group_daily_menu_item`에 “메뉴명 리스트”로 저장됩니다.

앱 사용 목록에 “핀 메뉴” 기능이 없어서(현재 기준) 운영에서 실사용이 없으면 유지 비용만 발생할 수 있습니다.

### 관련 테이블/필드(근거)

- `default_group_menu`
  - `menu_group_id`, `menu_name`, `active`, `start_date`, `end_date`, `store_id`
- `group_daily_menu`, `group_daily_menu_item`
  - `menu_group_id`, `date`, `name`

### 정리 권장

- “핀 메뉴” 기능을 쓰지 않으면:
  - `default_group_menu` 테이블 + 관련 API/서비스 로직(전개 포함)은 Deprecated→제거 후보
  - 단, `group_daily_menu(_item)`은 일반 “그룹 일간 메뉴”에도 사용하므로 **테이블 자체는 유지**, “default 전개 경로”만 제거하는 방향이 안전합니다.

---

## 5) QR Store 목록 API와 데이터 정리(운영/관리 성격)

### 관측

- `/api/v1/qr/stores`는 `store_qr` 전체 조회 API입니다.
- 앱은 단건 조회(`/api/v1/qr/stores/{qrToken}`)만 사용.

### 관련 테이블/필드(근거)

- `store_qr`
  - `qr_token`(unique), `store_id`, `menu_group_id`, `is_active`

### 정리 권장

- 목록 API가 운영에서 필요 없다면: API 제거(또는 ADMIN 권한 제한) + DB는 유지
- `store_qr`에서 `menu_group_id`가 nullable인데, “항상 특정 그룹으로 QR이 고정되어야 한다”면 nullable 정책 재검토(마이그레이션 필요)

---

## 최종 우선순위 추천 (DB 관점)

1) **관리자 계정 저장소 단일화**  
   - `admin` 테이블 폐기 또는 `accounts/admin_profiles` 폐기 중 택1 (권장: `admin` 폐기)

2) **남은 수량 정본 단일화**  
   - `store.remain` vs `menu_group_stock.stock` 중 택1 (권장: `menu_group_stock.stock`)

3) **`daily_menu.stock` 정리**  
   - 그룹 재고 정본 확정 시 `daily_menu.stock`와 관련 메서드/쿼리 정리

