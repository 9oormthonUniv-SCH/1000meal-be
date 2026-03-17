## 도메인 병합 관점 보고서 (분산 구조 정리 중심)

### 작성 목적

현재 프로젝트는 기능 자체는 동작하지만, **도메인/테이블/엔드포인트가 여러 축으로 분산**되어 있습니다. 그 결과:

- “정본(source of truth)”이 둘 이상 존재 (`store.remain` vs `menu_group_stock.stock`, `admin` vs `accounts/admin_profiles`)
- 같은 일을 하는 API가 여러 군데에 존재 (`/api/*` vs `/api/v1/auth/*`, 이메일 변경 플로우 2종)
- 메뉴/재고/알림/QR이 서로 다른 기준으로 결합되어 변경 영향 범위가 커짐

따라서 이번 보고서는 **도메인 병합(모듈 경계 재정의 + DB 정본 단일화)** 관점에서 방향을 제시합니다.

---

## 1) 병합 판단 기준 (우선순위 높은 순)

### 1.1 정본 단일화가 가능한가?
- “남은 수량/재고/운영상태” 같이 핵심 상태는 정본을 반드시 1개로 둡니다.
- 정본이 2개 이상이면 병합의 1순위 대상입니다.

### 1.2 동일 기능의 API/도메인이 중복되는가?
- 동일 유스케이스(로그인/비번변경/이메일변경/관리자 로그인 등)를 서로 다른 컨트롤러/테이블이 처리하면 병합 대상입니다.

### 1.3 경계가 “조회/명령”으로 나뉘지 않고 서로 침범하는가?
- 예: 메뉴 조회에서 기본 메뉴 규칙을 전개(materialize)해서 DB를 쓰는 경우.
- 병합을 통해 “읽기 모델/쓰기 모델”을 분리하거나, 최소한 쓰기 책임을 한 곳으로 모읍니다.

### 1.4 운영 리스크(장애/데이터 불일치)가 큰가?
- “가입은 되는데 로그인 안 됨” 같은 구조적 리스크는 병합 우선순위가 매우 높습니다.

---

## 2) 병합 제안: 목표 아키텍처(도메인 경계)

### 2.1 권장 최종 도메인(모듈) 구성

- **Identity (인증/계정/프로필)**  
  - 로그인/회원가입/토큰/권한/프로필(학생/관리자)/이메일 인증/비밀번호 재설정
  - “관리자”도 같은 계정 체계에서 role로만 구분 (ADMIN/STUDENT)

- **Store (매장)**  
  - 매장 기본 정보, 영업 상태, 매장 이미지, 매장-관리자 소속(관리자 프로필의 store_id)

- **Menu (메뉴 & 그룹)**  
  - 메뉴 그룹(기본 그룹 포함), 일자별 그룹 메뉴(`group_daily_menu`)  
  - “기본(핀) 메뉴 규칙”이 유지된다면 여기로 편입(규칙/전개 로직의 명확화)

- **Stock (재고)**  
  - 재고 정본은 `menu_group_stock` 1개로 고정
  - 재고 초기화(00:00), 종료 시점(12:00) 0 처리, 차감(QR/관리자) 모두 이 도메인 명령으로 통일

- **QR (사용/명부/QR 정의)**  
  - QR 토큰→매장/그룹 매핑, 사용 기록(명부), 차감 트리거는 Stock으로 위임

- **Notice (공지)**  
  - 공지/이미지 관리만 담당 (링크 활성화는 클라이언트 렌더링 정책)

- **Notification (FCM)**  
  - 전송/설정/히스토리/정책  
  - “재고 기반 알림”은 Stock에서 제공하는 조회 모델로만 의존 (store.remain fallback 제거)

---

## 3) 병합의 핵심: DB 정본 단일화(테이블 구조 제안)

아래는 “기능 분산을 줄이는” 관점에서의 **추천 테이블 구조**입니다.  
(현재 테이블을 한 번에 갈아엎기보다, 마이그레이션 단계를 밟는 것을 전제합니다.)

### 3.1 Identity 병합 (관리자 계정 단일화)

#### 문제
- 현재 관리자 계정 저장소가 `admin` 테이블과 `accounts/admin_profiles`로 이원화되어 있음.

#### 목표
- **관리자도 `accounts`를 정본으로 사용**하고, 관리자 특화 정보는 `admin_profiles`로 유지.
- `admin` 테이블은 마이그레이션 후 폐기.

#### 제안 구조
- `accounts` (정본)
  - `id (PK)`
  - `user_id` (unique)
  - `email` (unique)
  - `password_hash`
  - `role` (STUDENT/ADMIN)
  - `status` (ACTIVE/DELETED/...)
- `user_profiles` (학생)
  - `account_id (PK/FK -> accounts.id)`
  - 학생 표시 정보
- `admin_profiles` (관리자)
  - `account_id (PK/FK -> accounts.id)`
  - `store_id (FK -> store.id)`  ← “관리자 소속 매장”
  - `display_name`, `admin_level`

#### 정리 대상(병합 완료 후)
- `admin` 테이블
- `AdminEntity/AdminRepository/AdminService`가 담당하던 DB 흐름(가입/비번변경)
- `/api/*` 관리자 엔드포인트는 `/api/v1/auth/*`로 통합하거나 `/api/v1/admin/*`로 재배치

---

### 3.2 Stock 정본 단일화 (`store.remain` 제거 방향)

#### 문제
- 재고 정본이 `menu_group_stock.stock`인데, `store.remain`이 fallback로 남아있음.

#### 목표
- **재고 정본 = `menu_group_stock.stock`만 사용**
- store 레벨 remain은 파생값(뷰/캐시)로만 취급하거나 제거.

#### 제안 구조
- `menu_group_stock` (정본)
  - `menu_group_id (PK or unique FK)`
  - `stock`
  - `capacity`
  - (알림 관련) `last_notified_*`
- `store`에서 `remain` 컬럼은 제거(또는 read model로 분리)
  - 필요하면 “대표 그룹(기본 그룹)”의 재고를 store remain처럼 보여줌

#### 정리 대상
- `store.remain` (컬럼) 및 이를 참조하는 fallback 로직

---

### 3.3 Menu 구조 병합 (DailyMenu의 재고/메뉴 엔티티 이중화 정리)

#### 문제
- `DailyMenu.stock`(레거시 느낌)과 `MenuGroupStock.stock`(실사용 정본)이 혼재
- `DailyMenu.menus (Menu 엔티티)`와 `GroupDailyMenu.menuNames(문자열 컬렉션)`의 이중 구조

#### 목표
- **재고는 그룹 재고만** 유지
- 메뉴 구성은 “그룹 단위 일자 메뉴”를 정본으로 통일 (현재 앱/QR/알림 흐름과 가장 일치)

#### 제안 구조(정본 후보)
- `menu_group` (그룹 정의)
  - `id`, `store_id`, `name`, `sort_order`, `is_default`, (선택) `daily_menu_id`
- `group_daily_menu` (일자별 그룹 메뉴)
  - `id`, `menu_group_id`, `date`
- `group_daily_menu_item` (일자별 메뉴 아이템)
  - `group_daily_menu_id`, `name`

#### 정리 대상(정책 확정 후 단계적)
- `daily_menu.stock` 컬럼 및 관련 쿼리
- `menu` 테이블(엔티티) 라인이 레거시라면 제거 후보
- `DailyMenu.toDto()` 같이 사용처 없는 변환 로직

---

### 3.4 Email 변경 플로우 병합 (API 라인 통일)

#### 문제
- 이메일 변경이 `/api/v1/auth/email/change/*` 와 `/api/v1/account/email/*`로 이중화

#### 목표
- **이메일 변경 플로우는 하나로 통일**
- `email_verification_tokens`는 공용(회원가입/이메일 변경/비번재설정 등) 인프라 테이블로 유지

#### 제안 구조
- `email_verification_tokens`
  - `email`, `code`, `verified`, `expires_at`, `created_at`
- 이메일 변경 API는 `/api/v1/account/email/*`로 단일화(현재 앱 문서 기준)

#### 정리 대상
- `/api/v1/auth/email/change/request`, `/api/v1/auth/email/change/confirm` (Deprecated→삭제)

---

## 4) 병합 로드맵(권장 순서)

1) **관리자 계정 저장소 단일화** (`admin` → `accounts/admin_profiles`)
2) **재고 정본 단일화** (`store.remain` fallback 제거, `menu_group_stock` 기반으로 FCM/조회 통일)
3) **Menu 정본 통일** (`DailyMenu.stock`/`Menu` 엔티티 레거시 정리)
4) **이메일 변경 API 라인 통합**

---

## 5) 병합 이후 기대 효과

- “어디가 정본인지”가 명확해져 장애/혼선 감소
- 신규 기능 추가 시 영향 범위 축소(특히 재고/알림/QR)
- API/테이블 중복 제거로 유지보수 비용 감소
- 데이터 마이그레이션/정합성 검증이 쉬워짐

