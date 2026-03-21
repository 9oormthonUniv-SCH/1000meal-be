-- 요일별 메뉴 그룹 기본 수량 (자정 리셋 시 사용)
CREATE TABLE menu_group_capacity_by_day (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_group_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_group_capacity_by_day (menu_group_id, day_of_week),
    CONSTRAINT fk_menu_group_capacity_by_day_group FOREIGN KEY (menu_group_id)
        REFERENCES menu_group (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- menu_group_id 1~4: 월~금 80개 / 5: 월~목 80개, 금 60개. 토·일 데이터 없음. 중복 시 무시(INSERT IGNORE).
INSERT IGNORE INTO menu_group_capacity_by_day (menu_group_id, day_of_week, capacity)
SELECT d.gid, d.dow, d.cap
FROM (
    SELECT 1 AS gid, 'MONDAY'    AS dow, 80 AS cap
    UNION ALL SELECT 1, 'TUESDAY',   80
    UNION ALL SELECT 1, 'WEDNESDAY', 80
    UNION ALL SELECT 1, 'THURSDAY',  80
    UNION ALL SELECT 1, 'FRIDAY',    80
    UNION ALL SELECT 2, 'MONDAY',    80
    UNION ALL SELECT 2, 'TUESDAY',   80
    UNION ALL SELECT 2, 'WEDNESDAY', 80
    UNION ALL SELECT 2, 'THURSDAY',  80
    UNION ALL SELECT 2, 'FRIDAY',    80
    UNION ALL SELECT 3, 'MONDAY',    80
    UNION ALL SELECT 3, 'TUESDAY',   80
    UNION ALL SELECT 3, 'WEDNESDAY', 80
    UNION ALL SELECT 3, 'THURSDAY',  80
    UNION ALL SELECT 3, 'FRIDAY',    80
    UNION ALL SELECT 4, 'MONDAY',    80
    UNION ALL SELECT 4, 'TUESDAY',   80
    UNION ALL SELECT 4, 'WEDNESDAY', 80
    UNION ALL SELECT 4, 'THURSDAY',  80
    UNION ALL SELECT 4, 'FRIDAY',    80
    UNION ALL SELECT 5, 'MONDAY',    80
    UNION ALL SELECT 5, 'TUESDAY',   80
    UNION ALL SELECT 5, 'WEDNESDAY', 80
    UNION ALL SELECT 5, 'THURSDAY',  80
    UNION ALL SELECT 5, 'FRIDAY',    60
) d
WHERE EXISTS (SELECT 1 FROM menu_group mg WHERE mg.id = d.gid);
