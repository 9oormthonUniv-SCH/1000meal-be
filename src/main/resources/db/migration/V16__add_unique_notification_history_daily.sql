ALTER TABLE notification_history
    ADD COLUMN menu_group_id_norm BIGINT
        GENERATED ALWAYS AS (IFNULL(menu_group_id, 0)) STORED;

ALTER TABLE notification_history
    ADD COLUMN week_key_norm VARCHAR(20)
        GENERATED ALWAYS AS (IFNULL(week_key, '')) STORED;

CREATE UNIQUE INDEX uq_notification_history_daily_norm
    ON notification_history (sent_date, account_id, type, store_id, menu_group_id_norm, week_key_norm);
