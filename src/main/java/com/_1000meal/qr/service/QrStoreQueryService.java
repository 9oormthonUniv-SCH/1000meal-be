package com._1000meal.qr.service;

import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomNotFoundException;
import com._1000meal.menu.domain.MenuGroup;
import com._1000meal.menu.repository.MenuGroupRepository;
import com._1000meal.qr.api.dto.QrStoreResponse;
import com._1000meal.qr.domain.StoreQr;
import com._1000meal.qr.repository.StoreQrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QrStoreQueryService {

    private final StoreQrRepository storeQrRepository;
    private final MenuGroupRepository menuGroupRepository;

    public QrStoreResponse getByQrToken(String qrToken) {
        StoreQr storeQr = storeQrRepository.findActiveByQrTokenWithStore(qrToken)
                .orElseThrow(() -> new CustomNotFoundException(ErrorCode.QR_TOKEN_NOT_FOUND));

        MenuGroup menuGroup = resolveMenuGroup(storeQr.getMenuGroupId());
        return toResponse(storeQr, menuGroup);
    }

    public List<QrStoreResponse> getAll() {
        List<StoreQr> storeQrs = storeQrRepository.findAllWithStore();
        Map<Long, MenuGroup> menuGroupMap = loadMenuGroupMap(storeQrs);

        return storeQrs.stream()
                .map(storeQr -> toResponse(storeQr, menuGroupMap.get(storeQr.getMenuGroupId())))
                .toList();
    }

    private MenuGroup resolveMenuGroup(Long menuGroupId) {
        if (menuGroupId == null) {
            return null;
        }
        return menuGroupRepository.findById(menuGroupId).orElse(null);
    }

    private Map<Long, MenuGroup> loadMenuGroupMap(List<StoreQr> storeQrs) {
        List<Long> menuGroupIds = storeQrs.stream()
                .map(StoreQr::getMenuGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (menuGroupIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return menuGroupRepository.findByIdIn(new ArrayList<>(menuGroupIds))
                .stream()
                .collect(Collectors.toMap(MenuGroup::getId, Function.identity()));
    }

    private QrStoreResponse toResponse(StoreQr storeQr, MenuGroup menuGroup) {
        return new QrStoreResponse(
                storeQr.getStore().getId(),
                storeQr.getStore().getName(),
                storeQr.getMenuGroupId(),
                menuGroup != null ? menuGroup.getName() : null,
                storeQr.getQrToken(),
                storeQr.isActive()
        );
    }
}
