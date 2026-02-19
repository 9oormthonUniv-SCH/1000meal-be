CREATE TABLE meal_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    used_at DATETIME(6) NOT NULL,
    used_date DATE NOT NULL,
    dept_snapshot VARCHAR(100) NOT NULL,
    student_no_snapshot VARCHAR(50) NOT NULL,
    name_snapshot VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_meal_usage_user_date UNIQUE (user_id, used_date),
    INDEX idx_meal_usage_used_date (used_date),
    INDEX idx_meal_usage_store_date (store_id, used_date),
    CONSTRAINT fk_meal_usage_user FOREIGN KEY (user_id) REFERENCES accounts(id),
    CONSTRAINT fk_meal_usage_store FOREIGN KEY (store_id) REFERENCES store(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 검증용 SQL
-- 1) 같은 user_id + used_date 중복 INSERT 실패 확인
-- INSERT INTO meal_usage (user_id, store_id, used_at, used_date, dept_snapshot, student_no_snapshot, name_snapshot)
-- VALUES (1, 1, NOW(6), CURDATE(), 'CS', '20241234', '홍길동');
-- INSERT INTO meal_usage (user_id, store_id, used_at, used_date, dept_snapshot, student_no_snapshot, name_snapshot)
-- VALUES (1, 1, NOW(6), CURDATE(), 'CS', '20241234', '홍길동');
