-- Remove historical duplicates by normalized dedup key.
WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY
                type,
                account_id,
                store_id,
                sent_date,
                IFNULL(menu_group_id, 0),
                IFNULL(week_key, '')
            ORDER BY id ASC
        ) AS rn
    FROM notification_history
)
DELETE FROM notification_history
WHERE id IN (SELECT id FROM ranked WHERE rn > 1);

-- Drop old unique indexes that do not dedupe when nullable columns are NULL.
DROP INDEX uk_notification_history_type_account_store_date ON notification_history;
DROP INDEX uq_notification_history_daily ON notification_history;

-- Add normalized generated columns used by dedup guard.
ALTER TABLE notification_history
    ADD COLUMN dedup_menu_group_id BIGINT GENERATED ALWAYS AS (IFNULL(menu_group_id, 0)) STORED,
    ADD COLUMN dedup_week_key VARCHAR(20) GENERATED ALWAYS AS (IFNULL(week_key, '')) STORED;

-- One row per normalized key, safe under concurrency and multi-instance.
CREATE UNIQUE INDEX uq_notification_history_guard
ON notification_history (
    type,
    account_id,
    store_id,
    sent_date,
    dedup_menu_group_id,
    dedup_week_key
);
