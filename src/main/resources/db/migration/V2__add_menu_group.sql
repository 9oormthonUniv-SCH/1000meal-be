-- ============================================
-- 메뉴 그룹 재고 시스템 마이그레이션 스크립트
-- ============================================
-- 실행 방법:
--   1. 테스트 환경: ./gradlew bootRun으로 JPA ddl-auto=update가 테이블 생성
--   2. 운영 환경: 아래 SQL을 직접 실행하거나 Flyway 적용
--
-- 주의사항:
--   - 기존 데이터가 있는 경우 "기본 메뉴" 그룹으로 자동 마이그레이션
--   - daily_menu.stock 값이 menu_group_stock.stock으로 복사됨
-- ============================================

-- Step 1: menu_group 테이블 생성 (JPA ddl-auto=update로 자동 생성될 예정)
-- CREATE TABLE IF NOT EXISTS menu_group (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     daily_menu_id BIGINT NOT NULL,
--     name VARCHAR(100) NOT NULL,
--     sort_order INT NOT NULL DEFAULT 0,
--     CONSTRAINT fk_menu_group_daily_menu FOREIGN KEY (daily_menu_id) REFERENCES daily_menu(id) ON DELETE CASCADE
-- );
-- CREATE INDEX idx_menu_group_daily_menu ON menu_group(daily_menu_id);

-- Step 2: menu_group_stock 테이블 생성 (JPA ddl-auto=update로 자동 생성될 예정)
-- CREATE TABLE IF NOT EXISTS menu_group_stock (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     menu_group_id BIGINT NOT NULL UNIQUE,
--     stock INT NOT NULL DEFAULT 100,
--     capacity INT NOT NULL DEFAULT 100,
--     low_stock_notified TINYINT(1) NOT NULL DEFAULT 0,
--     CONSTRAINT fk_menu_group_stock_group FOREIGN KEY (menu_group_id) REFERENCES menu_group(id) ON DELETE CASCADE
-- );

-- Step 3: menu 테이블에 menu_group_id 컬럼 추가 (JPA ddl-auto=update로 자동 생성될 예정)
-- ALTER TABLE menu ADD COLUMN menu_group_id BIGINT;
-- ALTER TABLE menu ADD CONSTRAINT fk_menu_menu_group FOREIGN KEY (menu_group_id) REFERENCES menu_group(id);
-- CREATE INDEX idx_menu_group ON menu(menu_group_id);

-- ============================================
-- 데이터 마이그레이션 (기존 데이터가 있는 경우 실행)
-- ============================================

-- Step 4: 기존 daily_menu마다 "기본 메뉴" 그룹 생성
INSERT INTO menu_group (daily_menu_id, name, sort_order)
SELECT id, '기본 메뉴', 0 FROM daily_menu
WHERE id NOT IN (SELECT DISTINCT daily_menu_id FROM menu_group);

-- Step 5: menu_group_stock에 기존 daily_menu.stock 복사
INSERT INTO menu_group_stock (menu_group_id, stock, capacity, low_stock_notified)
SELECT mg.id, COALESCE(dm.stock, 100), COALESCE(dm.stock, 100), 0
FROM menu_group mg
JOIN daily_menu dm ON mg.daily_menu_id = dm.id
WHERE mg.id NOT IN (SELECT menu_group_id FROM menu_group_stock);

-- Step 6: menu.menu_group_id 연결 (기존 메뉴를 기본 그룹에 연결)
UPDATE menu m
JOIN daily_menu dm ON m.daily_menu_id = dm.id
JOIN menu_group mg ON mg.daily_menu_id = dm.id AND mg.name = '기본 메뉴'
SET m.menu_group_id = mg.id
WHERE m.menu_group_id IS NULL;

-- Step 7: (선택) menu.menu_group_id를 NOT NULL로 변경
-- 마이그레이션 완료 후 모든 menu에 menu_group_id가 설정되면 실행
-- ALTER TABLE menu MODIFY COLUMN menu_group_id BIGINT NOT NULL;
