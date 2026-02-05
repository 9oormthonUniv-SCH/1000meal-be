-- ============================================
-- 기본(핀) 메뉴 규칙 테이블 생성
-- ============================================

CREATE TABLE IF NOT EXISTS default_group_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    menu_group_id BIGINT NOT NULL,
    menu_name VARCHAR(80) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    created_by_account_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_default_group_menu_store
        FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    CONSTRAINT fk_default_group_menu_group
        FOREIGN KEY (menu_group_id) REFERENCES menu_group(id) ON DELETE CASCADE,
    CONSTRAINT uk_default_group_menu
        UNIQUE (menu_group_id, menu_name, start_date, end_date)
);

CREATE INDEX idx_default_group_menu_group ON default_group_menu(menu_group_id);
CREATE INDEX idx_default_group_menu_range ON default_group_menu(menu_group_id, start_date, end_date);
