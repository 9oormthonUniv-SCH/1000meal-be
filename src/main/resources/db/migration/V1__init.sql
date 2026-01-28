-- Initial schema for MySQL 8.0 (Flyway V1)

CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_accounts_user_id (user_id),
    UNIQUE KEY uk_accounts_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE store (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_url VARCHAR(1024),
    name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(255),
    description VARCHAR(255),
    open_time TIME(6),
    close_time TIME(6),
    is_open BIT(1) NOT NULL,
    remain INT NOT NULL,
    hours VARCHAR(255),
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notice (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    is_published BIT(1) NOT NULL,
    is_pinned BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE email_verification_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(6) NOT NULL,
    verified BIT(1) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_email_verified (email, verified),
    INDEX idx_email_created (email, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fcm_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    active BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fcm_tokens_token UNIQUE (token),
    INDEX idx_fcm_tokens_account_active (account_id, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    enabled BIT(1) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notification_preferences_account UNIQUE (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE weekly_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_date DATE,
    end_date DATE,
    store_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_weekly_menu_store FOREIGN KEY (store_id) REFERENCES store(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE daily_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT,
    weekly_menu_id BIGINT,
    date DATE,
    day_of_week VARCHAR(255),
    is_open BIT(1) NOT NULL,
    is_holiday BIT(1) NOT NULL,
    stock INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_daily_menu_store FOREIGN KEY (store_id) REFERENCES store(id),
    CONSTRAINT fk_daily_menu_weekly_menu FOREIGN KEY (weekly_menu_id) REFERENCES weekly_menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    daily_menu_id BIGINT,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL,
    is_default BIT(1) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_menu_group_daily_menu (daily_menu_id),
    INDEX idx_menu_group_store_id (store_id),
    CONSTRAINT fk_menu_group_store FOREIGN KEY (store_id) REFERENCES store(id),
    CONSTRAINT fk_menu_group_daily_menu FOREIGN KEY (daily_menu_id) REFERENCES daily_menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu_group_stock (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_group_id BIGINT NOT NULL,
    stock INT NOT NULL,
    capacity INT NOT NULL,
    last_notified_threshold INT,
    last_notified_date DATE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_group_stock_menu_group (menu_group_id),
    CONSTRAINT fk_menu_group_stock_menu_group FOREIGN KEY (menu_group_id) REFERENCES menu_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    daily_menu_id BIGINT NOT NULL,
    menu_group_id BIGINT,
    PRIMARY KEY (id),
    INDEX idx_menu_group (menu_group_id),
    CONSTRAINT fk_menu_daily_menu FOREIGN KEY (daily_menu_id) REFERENCES daily_menu(id),
    CONSTRAINT fk_menu_menu_group FOREIGN KEY (menu_group_id) REFERENCES menu_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE group_daily_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_group_id BIGINT NOT NULL,
    date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_group_daily_menu UNIQUE (menu_group_id, date),
    CONSTRAINT fk_gdm_menu_group FOREIGN KEY (menu_group_id) REFERENCES menu_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE group_daily_menu_item (
    group_daily_menu_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    CONSTRAINT fk_gdmi_gdm FOREIGN KEY (group_daily_menu_id) REFERENCES group_daily_menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE favorite_store (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_favorite_store_account_store UNIQUE (account_id, store_id),
    INDEX idx_favorite_store_store (store_id),
    INDEX idx_favorite_store_account (account_id),
    CONSTRAINT fk_favorite_store_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_favorite_store_store FOREIGN KEY (store_id) REFERENCES store(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    department VARCHAR(100),
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_profiles_account UNIQUE (account_id),
    CONSTRAINT fk_user_profiles_account FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    admin_level INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_admin_profiles_account UNIQUE (account_id),
    CONSTRAINT fk_admin_profiles_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_admin_profiles_store FOREIGN KEY (store_id) REFERENCES store(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE password_reset_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_prt_token_hash (token_hash),
    INDEX idx_prt_account (account_id),
    CONSTRAINT fk_password_reset_token_account FOREIGN KEY (account_id) REFERENCES accounts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
