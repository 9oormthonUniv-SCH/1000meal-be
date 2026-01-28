-- ============================================
-- GroupDailyMenu: 메뉴 그룹별 일일 메뉴 저장
-- ============================================
-- 메뉴 그룹(menu_group)과 날짜(date) 조합으로
-- 메뉴 이름 목록을 저장하는 새로운 테이블입니다.
-- UNIQUE(menu_group_id, date) 제약으로 동일 그룹+날짜 중복 방지.
-- ============================================

-- Step 1: group_daily_menu 테이블 생성
CREATE TABLE group_daily_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_group_id BIGINT NOT NULL,
    date DATE NOT NULL,
    UNIQUE KEY uk_group_daily_menu (menu_group_id, date),
    CONSTRAINT fk_gdm_menu_group FOREIGN KEY (menu_group_id) REFERENCES menu_group(id)
);

-- Step 2: group_daily_menu_item 테이블 생성 (@ElementCollection 매핑)
CREATE TABLE group_daily_menu_item (
    group_daily_menu_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    CONSTRAINT fk_gdmi_gdm FOREIGN KEY (group_daily_menu_id) REFERENCES group_daily_menu(id)
);
