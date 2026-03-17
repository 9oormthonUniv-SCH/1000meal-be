# 프론트(Flutter 앱) API 사용 목록

이 문서는 **현재 앱 코드에서 실제로 호출하는 API**를 정리한 것입니다.

## Base URL (내부 API)

- **API Root (도메인)**: `NEXT_PUBLIC_API_URL` (env: `assets/env/.env`)
- **API Base (v1)**: `${NEXT_PUBLIC_API_URL}/api/v1`

관련 코드:
- `lib/common/config/app_config.dart` (`AppConfig.apiBaseUrl`)
- `lib/common/dio/dio_client.dart` (`DioClient.create()`의 `baseUrl`)

---

## 내부 API (백엔드: `${apiBaseUrl}` 기준)

### Auth / Account / FCM (`lib/features/auth/data/auth_api.dart`)

| Method | Path | Auth | 용도 |
|---|---|---|---|
| POST | `/auth/login` | - | 로그인 |
| POST | `/auth/refresh` | - | Refresh Token으로 Access Token 재발급 |
| GET | `/auth/me` | Bearer | 내 정보 조회 |
| POST | `/auth/signup` | - | 회원가입 |
| POST | `/signup/user/validate-id` | - | 회원가입: 아이디 중복/유효성 검증 |
| GET | `/auth/email/status` | - | 회원가입 이메일 상태 조회 (`?email=`) |
| POST | `/auth/email/send` | - | 회원가입 이메일 인증코드 발송 |
| POST | `/auth/email/verify` | - | 회원가입 이메일 인증코드 검증 |
| POST | `/auth/find-id` | - | 아이디 찾기 |
| POST | `/auth/password/reset/request` | - | 비밀번호 재설정 요청 |
| POST | `/auth/password/reset/confirm` | - | 비밀번호 재설정 확정 |
| POST | `/auth/delete-account` | Bearer | 회원탈퇴 |
| POST | `/account/email/start` | Bearer | 이메일 변경 1단계(비밀번호 확인, changeId 발급) |
| POST | `/account/email/code` | Bearer | 이메일 변경 2단계(새 이메일로 인증코드 발송) |
| POST | `/account/email/verify` | Bearer | 이메일 변경 3단계(인증코드 검증/변경 완료) |
| POST | `/fcm/tokens` | Bearer | FCM 토큰 등록 |
| GET | `/fcm/preferences` | Bearer | 알림 설정 조회 |
| PATCH | `/fcm/preferences` | Bearer | 알림 설정 변경 |

### Store / Favorites (`lib/features/store/data/store_api.dart`)

| Method | Path | Auth | 용도 |
|---|---|---|---|
| GET | `/stores` | - | 매장 목록(지도/홈 등에서 사용) |
| GET | `/stores/{id}` | - | 매장 상세 |
| GET | `/favorites/stores` | Bearer | 즐겨찾기 매장 ID 목록 |
| POST | `/favorites/stores/{storeId}` | Bearer | 즐겨찾기 추가(토글) |
| DELETE | `/favorites/stores/{storeId}` | Bearer | 즐겨찾기 제거(토글) |

### Admin (영업상태/메뉴/재고/프리셋) (`lib/features/admin/data/admin_api.dart`)

| Method | Path | Auth | 용도 |
|---|---|---|---|
| GET | `/stores/{storeId}` | Bearer | (관리자) 내 매장 상세 조회 |
| POST | `/stores/status/{storeId}` | Bearer | (관리자) 영업 상태 토글 |
| GET | `/menus/daily/{storeId}/groups` | Bearer | (관리자) 일간 메뉴 그룹 조회 (`?date=`) |
| GET | `/menus/daily/weekly/{storeId}/groups` | Bearer | (관리자) 주간 메뉴 그룹 조회 (`?date=`) |
| POST | `/menus/daily/{storeId}/groups` | Bearer | (관리자) 메뉴 그룹 생성 (`name/sortOrder/capacity`) |
| POST | `/stores/{storeId}/menus/daily/groups/{groupId}/menus` | Bearer | (관리자) 메뉴 그룹의 메뉴 목록 upsert (`?date=`) |
| POST | `/stores/{storeId}/menus/daily/groups/{groupId}/stock` | Bearer | (관리자) 메뉴 그룹 재고 설정 |
| POST | `/stores/{storeId}/menus/daily/groups/{groupId}/deduct` | Bearer | (관리자) 메뉴 그룹 재고 차감 (`?deductionUnit=`) |
| DELETE | `/menus/daily/groups/{groupId}` | Bearer | (관리자) 메뉴 그룹 삭제 |
| POST | `/menus/daily/group/{menuId}/stock` | Bearer | (레거시) 일간 메뉴 단위 재고 설정 |
| GET | `/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets` | Bearer | (관리자) 메뉴 프리셋 목록 |
| GET | `/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets/{presetId}` | Bearer | (관리자) 메뉴 프리셋 상세 |
| POST | `/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets` | Bearer | (관리자) 메뉴 프리셋 생성 |
| DELETE | `/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets/{presetId}` | Bearer | (관리자) 메뉴 프리셋 삭제 |

> 참고: `AdminApi.saveDailyMenu()`가 `POST /menus/daily/{storeId}/groups?date=` 형태로도 호출합니다(레거시 호환).

### Notice (공지) (`lib/features/notice/data/notice_api.dart`)

| Method | Path | Auth | 용도 |
|---|---|---|---|
| GET | `/notices` | (옵션) Bearer | 공지 목록 조회 |
| GET | `/notices/{id}` | (옵션) Bearer | 공지 상세 조회 |
| POST | `/notices` | Bearer | 공지 생성 |
| PUT | `/notices/{id}` | Bearer | 공지 수정 |
| DELETE | `/notices/{id}` | Bearer | 공지 삭제 |
| POST | `/notices/{id}/images/presign` | Bearer | 공지 이미지 업로드용 presigned URL 발급 |
| POST | `/notices/{id}/images` | Bearer | 업로드 완료 이미지 등록(메타/URL 등록) |

### QR (`lib/features/qr/data/qr_api.dart`)

| Method | Path | Auth | 용도 |
|---|---|---|---|
| GET | `/qr/usages/today` | Bearer | 당일 명부 등록 여부 조회(404면 미등록) |
| GET | `/qr/stores/{qrToken}` | Bearer | qrToken으로 매장/메뉴그룹 이름 조회 |
| POST | `/qr/usages` | Bearer | qrToken으로 명부 등록 |

---

## 외부/비정형 호출 (백엔드가 아닌 대상 포함)

### S3 Presigned URL 업로드 (`lib/features/notice/data/notice_api.dart`)

- **Method**: `PUT` (기본) 또는 `POST`
- **URL**: 백엔드가 내려준 **presigned URL (전체 URL)**  
- **용도**: 공지 이미지 바이트를 S3(또는 호환 스토리지)에 직접 업로드

### QR 스캔 URL 기반 POST (`lib/features/qr/data/qr_api.dart`)

- **Method**: `POST`
- **URL**: 사용자가 스캔한 QR URL의 `origin + path` (예: `https://domain/api/v1/qr/usages`)
- **용도**: QR에서 `qrToken`을 추출해 스캔 URL이 가리키는 endpoint로 명부 등록 요청
- **주의**: 이 경우 **도메인/경로가 고정이 아닐 수 있음** (QR에 인코딩된 URL에 의존)

