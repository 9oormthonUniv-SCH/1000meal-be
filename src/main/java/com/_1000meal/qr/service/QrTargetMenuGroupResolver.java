package com._1000meal.qr.service;

import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.menu.repository.GroupDailyMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QrTargetMenuGroupResolver {

    private final GroupDailyMenuRepository groupDailyMenuRepository;

    public Long resolveMenuGroupId(Long storeId, LocalDate usedDate) {
        List<Long> groupIds = groupDailyMenuRepository.findMenuGroupIdsByStoreIdAndDate(storeId, usedDate);

        if (groupIds.isEmpty()) {
            throw new CustomException(ErrorCode.CONFLICT, "오늘 메뉴가 없어 이용할 수 없습니다.");
        }
        if (groupIds.size() > 1) {
            throw new CustomException(ErrorCode.CONFLICT, "오늘 이용 그룹을 결정할 수 없습니다. 관리자에게 문의하세요.");
        }
        return groupIds.get(0);
    }
}
