CREATE UNIQUE INDEX uq_notification_history_daily
ON notification_history
(
    sent_date,
    account_id,
    type,
    store_id,
    menu_group_id,
    week_key
);
