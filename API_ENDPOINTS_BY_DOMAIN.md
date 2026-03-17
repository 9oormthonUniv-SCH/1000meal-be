# 1000원의 아침밥 API 엔드포인트 요약

- 기준: `api-docs (soonbob).json`
- 정리: 도메인(tag)별 method / path / 용도(summary 우선)

## Default Menu

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus` | 기본(핀) 메뉴 조회 |
| `POST` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus` | 기본(핀) 메뉴 설정 |
| `DELETE` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus` | 기본(핀) 메뉴 해제 |
| `PATCH` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus/{defaultMenuId}/activate` | 기본(핀) 메뉴 활성화 및 일간 메뉴 전개 |

## Account

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/auth/change-password` | 비밀번호 변경(로그인 필요) |
| `POST` | `/api/v1/auth/delete-account` | 회원 탈퇴(로그인 필요) |
| `POST` | `/api/v1/auth/email/change/confirm` | 이메일 변경 확정(인증코드 확인, 로그인 필요) |
| `POST` | `/api/v1/auth/email/change/request` | 이메일 변경 인증코드 발송(로그인 필요) |
| `POST` | `/api/v1/auth/find-id` | 아이디(학번) 찾기 |

## Signup - Validation

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/signup/user/validate-id` | 학번(ID) 중복 및 형식 검증 |

## Notice

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/notices` | 공지사항 목록 조회 |
| `POST` | `/api/v1/notices` | 공지사항 생성 |
| `GET` | `/api/v1/notices/{id}` | 공지사항 단건 조회 |
| `PUT` | `/api/v1/notices/{id}` | 공지사항 수정 |
| `DELETE` | `/api/v1/notices/{id}` | 공지사항 삭제(소프트 삭제) |
| `POST` | `/api/v1/notices/{id}/images` | 공지 이미지 메타 등록 |
| `POST` | `/api/v1/notices/{id}/images/presign` | 공지 이미지 Presigned URL 발급 |

## Admin Menu Group

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/admin/stores/{storeId}/menu-groups` | 매장별 메뉴 그룹 조회 (관리자) |

## Store

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/stores` | 매장 전체 목록 조회 |
| `POST` | `/api/v1/stores/image-url` | 매장 이미지 URL 설정 |
| `POST` | `/api/v1/stores/status/{storeId}` | 매장 운영 상태 토글 |
| `GET` | `/api/v1/stores/{storeId}` | 매장 상세 조회 |

## Admin Auth

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/login` | 관리자 로그인 |
| `GET` | `/api/me` | 내 관리자 정보 조회 |
| `PATCH` | `/api/password` | 관리자 비밀번호 변경 |
| `POST` | `/api/signup` | 관리자 회원가입 |

## Auth - Password

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/auth/password/reset/confirm` | 비밀번호 재설정 확정 |
| `POST` | `/api/v1/auth/password/reset/request` | 비밀번호 재설정 요청 |

## Menu Group

| Method | Path | 용도 |
|---|---|---|
| `DELETE` | `/api/v1/menus/daily/groups/{groupId}` | 메뉴 그룹 삭제 |
| `GET` | `/api/v1/menus/daily/weekly/{storeId}/groups` | 주간 메뉴 조회 |
| `GET` | `/api/v1/menus/daily/{storeId}/groups` | 일간 메뉴 그룹 조회 |
| `POST` | `/api/v1/menus/daily/{storeId}/groups` | 메뉴 그룹 생성 |
| `POST` | `/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/deduct` | 매장 기준 그룹 재고 차감 |
| `POST` | `/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/menus` | 매장 기준 그룹 메뉴 등록/교체 |
| `POST` | `/api/v1/stores/{storeId}/menus/daily/groups/{groupId}/stock` | 매장 기준 그룹 재고 직접 수정 |

## Admin Menu Preset

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets` | 자주 쓰는 메뉴 목록 조회 |
| `POST` | `/api/v1/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets` | 자주 쓰는 메뉴 생성 |
| `GET` | `/api/v1/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets/{presetId}` | 자주 쓰는 메뉴 상세 조회 |
| `DELETE` | `/api/v1/admin/stores/{storeId}/menus/daily/groups/{groupId}/menu-presets/{presetId}` | 자주 쓰는 메뉴 삭제 |

## Email Authentication

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/auth/email/send` | 이메일 인증 코드 발송 |
| `GET` | `/api/v1/auth/email/status` | 이메일 인증 상태 조회 |
| `POST` | `/api/v1/auth/email/verify` | 이메일 인증 코드 검증 |

## Account - Email

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/account/email/code` | 새 이메일로 인증 코드 발송 |
| `POST` | `/api/v1/account/email/start` | 이메일 변경 절차 시작 |
| `POST` | `/api/v1/account/email/verify` | 이메일 변경 인증 코드 검증 |

## Auth

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/auth/login` | 통합 로그인 |
| `POST` | `/api/v1/auth/logout` | 로그아웃 |
| `GET` | `/api/v1/auth/me` | 내 정보 조회 |
| `POST` | `/api/v1/auth/refresh` | Access Token 재발급 |
| `POST` | `/api/v1/auth/signup` | 통합 회원가입 |

## FCM

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/fcm/preferences` | 내 알림 설정 조회 |
| `PATCH` | `/api/v1/fcm/preferences` | 내 알림 설정 변경 |
| `POST` | `/api/v1/fcm/tokens` | FCM 토큰 등록/재연결 |

## favorite-store-controller

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/favorites/stores` | - |
| `POST` | `/api/v1/favorites/stores/{storeId}` | - |
| `DELETE` | `/api/v1/favorites/stores/{storeId}` | - |

## qr-store-query-controller

| Method | Path | 용도 |
|---|---|---|
| `GET` | `/api/v1/qr/stores` | - |
| `GET` | `/api/v1/qr/stores/{qrToken}` | - |

## qr-usage-controller

| Method | Path | 용도 |
|---|---|---|
| `POST` | `/api/v1/qr/usages` | - |
| `GET` | `/api/v1/qr/usages/today` | - |

