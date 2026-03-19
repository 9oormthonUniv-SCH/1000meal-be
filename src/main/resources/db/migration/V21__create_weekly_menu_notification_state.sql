CREATE TABLE weekly_menu_notification_state (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    menu_group_id BIGINT NOT NULL,
    week_key VARCHAR(20) NOT NULL,
    status ENUM('PENDING_LATE', 'SENT', 'CLOSED_NOT_SENT') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_weekly_menu_notification_state UNIQUE (store_id, menu_group_id, week_key),
    INDEX idx_weekly_menu_notification_state_status_week_key (status, week_key),
    CONSTRAINT fk_weekly_menu_notification_state_store
        FOREIGN KEY (store_id) REFERENCES store(id),
    CONSTRAINT fk_weekly_menu_notification_state_menu_group
        FOREIGN KEY (menu_group_id) REFERENCES menu_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
