-- 0) store_id 인덱스 먼저 추가 (FK가 요구하는 인덱스 대체)
CREATE INDEX idx_store_qr_store_id ON store_qr(store_id);

-- 1) 기존 store_id 단독 UNIQUE 제거
ALTER TABLE store_qr DROP INDEX uk_store_qr_store;

-- 2) menu_group_id 컬럼 추가 (이미 추가가 먼저라면 순서만 유지)
ALTER TABLE store_qr ADD COLUMN menu_group_id BIGINT NULL AFTER store_id;

-- 3) FK + 인덱스/유니크
ALTER TABLE store_qr
    ADD CONSTRAINT fk_store_qr_menu_group
        FOREIGN KEY (menu_group_id) REFERENCES menu_group(id);

CREATE INDEX idx_store_qr_store_group ON store_qr(store_id, menu_group_id);
CREATE UNIQUE INDEX uq_store_qr_store_group ON store_qr(store_id, menu_group_id);