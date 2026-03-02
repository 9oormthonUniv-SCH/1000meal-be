-- 1) 중복 제거
WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY
                sent_date,
                account_id,
                type,
                store_id,
                IFNULL(menu_group_id, 0),
                IFNULL(week_key, '')
            ORDER BY id ASC
        ) AS rn
    FROM notification_history
)
DELETE FROM notification_history
WHERE id IN (
    SELECT id FROM ranked WHERE rn > 1
);
