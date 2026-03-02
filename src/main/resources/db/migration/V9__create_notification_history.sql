CREATE TABLE notification_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(30) NOT NULL,
    account_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    sent_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notification_history_type_account_store_date
        UNIQUE (type, account_id, store_id, sent_date),
    INDEX idx_notification_history_account_sent_date (account_id, sent_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
