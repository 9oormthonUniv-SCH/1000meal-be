package com._1000meal.menu.event;

import java.time.LocalDate;
import java.util.List;


// 이미 해당 주에 메뉴 업로드 알림을 보낸 뒤, 메뉴가 수정된 경우 발행하는 이벤트
public record WeeklyMenuChangedEvent(
        Long storeId,
        List<Long> menuGroupIds,
        String weekKey,
        LocalDate weekStart
) {
}
