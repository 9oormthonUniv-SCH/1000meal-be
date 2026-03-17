# `API_ENDPOINTS_BY_DOMAIN.md`에만 있는 API 목록

- 비교 기준
  - `API_ENDPOINTS_BY_DOMAIN.md`: OpenAPI(`api-docs (soonbob).json`) 기반 전체 엔드포인트
  - `FRONTEND_API_USAGE.md`: Flutter 앱에서 실제 호출 목록 (Base: `${apiBaseUrl}` = `/api/v1`)
- 비교 키: **Method + Path**
- 주의: `FRONTEND_API_USAGE.md`의 Path는 `/api/v1`이 생략되어 있어, 비교 시 자동으로 `/api/v1` prefix를 붙여 정규화했습니다.

| Method | Path |
|---|---|
| `DELETE` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus` |
| `GET` | `/api/me` |
| `GET` | `/api/v1/admin/stores/{storeId}/menu-groups` |
| `GET` | `/api/v1/qr/stores` |
| `GET` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus` |
| `PATCH` | `/api/password` |
| `PATCH` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus/{defaultMenuId}/activate` |
| `POST` | `/api/login` |
| `POST` | `/api/signup` |
| `POST` | `/api/v1/auth/change-password` |
| `POST` | `/api/v1/auth/email/change/confirm` |
| `POST` | `/api/v1/auth/email/change/request` |
| `POST` | `/api/v1/auth/logout` |
| `POST` | `/api/v1/stores/image-url` |
| `POST` | `/api/v1/stores/{storeId}/menu-groups/{groupId}/default-menus` |

