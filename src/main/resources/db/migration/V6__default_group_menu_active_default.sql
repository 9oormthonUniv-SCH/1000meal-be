-- ============================================
-- default_group_menu.active 기본값을 0으로 변경
-- 기존 데이터는 유지
-- ============================================

ALTER TABLE default_group_menu
    MODIFY COLUMN active TINYINT(1) NOT NULL DEFAULT 0;
