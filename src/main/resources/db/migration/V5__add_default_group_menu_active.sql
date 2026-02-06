-- ============================================
-- 기본(핀) 메뉴 active 플래그 추가
-- 기존 데이터 호환을 위해 기본값은 활성(true)
-- ============================================

ALTER TABLE default_group_menu
    ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1;
