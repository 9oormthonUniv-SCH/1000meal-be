ALTER TABLE notification_history
    MODIFY COLUMN type ENUM(
    'open',
    'stock_deadline',
    'low_stock_30',
    'low_stock_10',
    'weekly_menu_uploaded'
    ) NOT NULL;