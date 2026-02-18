ALTER TABLE notification_history
    ADD COLUMN menu_group_id BIGINT NULL;

ALTER TABLE notification_history
    DROP INDEX uk_notification_history_type_account_store_date;

ALTER TABLE notification_history
    ADD UNIQUE INDEX uk_notification_history_type_account_store_date
        (type, account_id, store_id, menu_group_id, sent_date);
