-- 1) store_id 컬럼 추가 (우선 NULL 허용)
ALTER TABLE menu_group
    ADD COLUMN store_id BIGINT NULL;

-- 2) 기존 데이터 백필: menu_group.daily_menu_id -> daily_menu.store_id
UPDATE menu_group mg
    JOIN daily_menu dm ON dm.id = mg.daily_menu_id
    SET mg.store_id = dm.store_id
WHERE mg.store_id IS NULL;

-- 3) NULL 남아있으면 터지기 전에 확인용(선택)
-- SELECT COUNT(*) FROM menu_group WHERE store_id IS NULL;

-- 4) NOT NULL로 강화
ALTER TABLE menu_group
    MODIFY store_id BIGINT NOT NULL;

-- 5) 인덱스 + FK
ALTER TABLE menu_group
    ADD INDEX idx_menu_group_store_id (store_id),
  ADD CONSTRAINT fk_menu_group_store
    FOREIGN KEY (store_id) REFERENCES store(id);