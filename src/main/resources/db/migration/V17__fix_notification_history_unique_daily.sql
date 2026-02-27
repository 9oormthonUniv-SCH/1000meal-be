-- V17__dedupe_notification_history_and_add_unique_daily.sql

-- 1) 중복 제거: (sent_date, account_id, type, store_id, menu_group_id_norm, week_key_norm)
-- 가장 오래된 1개(id가 작은 것)만 남기고 삭제
WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
      PARTITION BY sent_date, account_id, type, store_id, menu_group_id_norm, week_key_norm
      ORDER BY id ASC
    ) AS rn
    FROM notification_history
)
DELETE FROM notification_history
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- 2) 유니크 인덱스 생성 (없을 때만)
-- MySQL은 CREATE INDEX IF NOT EXISTS가 없어서, 먼저 존재 확인 후 수동으로 처리해야 함.
-- Flyway SQL에서는 조건문이 제한적이므로, 아래는 "그냥 만들기"로 갑니다.
-- (현재 SHOW INDEX 결과가 Empty set 이었으니 실패 없이 생성될 것)

CREATE UNIQUE INDEX uq_notification_history_daily_norm
    ON notification_history (sent_date, account_id, type, store_id, menu_group_id_norm, week_key_norm);