-- V17__fix_notification_history_unique_daily.sql
-- 현재 DB에 menu_group_id_norm / week_key_norm 은 이미 존재한다고 가정
-- (테스트 서버에서 이미 생성됨)

-- 1) 인덱스 생성 전 중복 제거: 같은 키로 여러 건 있으면 최신 1건만 남김
WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
      PARTITION BY sent_date, account_id, type, store_id, menu_group_id_norm, week_key_norm
      ORDER BY created_at DESC, id DESC
    ) AS rn
    FROM notification_history
)
DELETE FROM notification_history
WHERE id IN (
    SELECT id FROM ranked WHERE rn >= 2
);

-- 2) 유니크 인덱스 생성 (아직 없는 상태)
CREATE UNIQUE INDEX uq_notification_history_daily_norm
    ON notification_history (sent_date, account_id, type, store_id, menu_group_id_norm, week_key_norm);