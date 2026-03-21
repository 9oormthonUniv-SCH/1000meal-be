-- V22 시드 오류 수정: menu_group_id 1, 2는 월~금 40개 (기존 80 → 40)
UPDATE menu_group_capacity_by_day
SET capacity = 40
WHERE menu_group_id IN (1, 2)
  AND day_of_week IN (
        'MONDAY',
        'TUESDAY',
        'WEDNESDAY',
        'THURSDAY',
        'FRIDAY'
    );
