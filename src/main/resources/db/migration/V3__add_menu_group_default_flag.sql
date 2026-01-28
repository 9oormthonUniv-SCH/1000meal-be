-- ============================================
-- 메뉴 그룹 기본 그룹 플래그 마이그레이션
-- ============================================
-- 모든 매장은 매장명과 동일한 이름의 기본 메뉴 그룹을 가진다.
-- 기본 그룹은 API에서 자동 생성되지 않으며, DB에서만 관리된다.
-- ============================================

-- Step 1: is_default 컬럼 추가
ALTER TABLE menu_group ADD COLUMN is_default TINYINT(1) NOT NULL DEFAULT 0;

-- Step 2: 기존 "기본 메뉴" 그룹을 기본 그룹으로 표시
UPDATE menu_group SET is_default = 1 WHERE name = '기본 메뉴';

-- Step 3: 기본 그룹 이름을 매장명으로 변경
UPDATE menu_group mg
JOIN daily_menu dm ON mg.daily_menu_id = dm.id
JOIN weekly_menu wm ON dm.weekly_menu_id = wm.id
JOIN store s ON wm.store_id = s.id
SET mg.name = s.name
WHERE mg.is_default = 1;
