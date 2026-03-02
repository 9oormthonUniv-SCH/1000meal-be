CREATE TABLE store_qr (
    id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    qr_token VARCHAR(100) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_store_qr_store UNIQUE (store_id),
    CONSTRAINT uk_store_qr_token UNIQUE (qr_token),
    CONSTRAINT fk_store_qr_store FOREIGN KEY (store_id) REFERENCES store(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
