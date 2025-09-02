package com._1000meal.favoriteMenu.service;

import com._1000meal.favoriteMenu.dto.FavoriteMenuDto;
import com._1000meal.favoriteMenu.dto.FavoriteMenuGroupBlock;
import com._1000meal.favoriteMenu.dto.FavoriteMenuGroupedResponse;
import com._1000meal.favoriteMenu.repository.FavoriteMenuGroupRepository;
import com._1000meal.favoriteMenu.repository.FavoriteMenuRepository;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.favoriteMenu.domain.FavoriteMenu;
import com._1000meal.favoriteMenu.domain.FavoriteMenuGroup;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteMenuService {

    private final StoreRepository storeRepository;
    private final FavoriteMenuGroupRepository groupRepository;
    private final FavoriteMenuRepository favoriteRepository;

    @Transactional
    public Long createGroupAndReplaceFavorites(Long storeId, List<String> names) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        // 1) 그룹 먼저 저장해서 PK 확보
        FavoriteMenuGroup group = groupRepository.save(
                FavoriteMenuGroup.builder().store(store).build()
        );

        // 2) 이름 정제
        List<String> cleaned = Optional.ofNullable(names).orElse(List.of()).stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();


        if (!cleaned.isEmpty()) {
            // 교체(save 전, 기존은 없지만 패턴 통일)
            favoriteRepository.deleteByGroup_Id(group.getId());

            // 문자열 하나하나 → FavoriteMenu 엔티티로 매핑
            List<FavoriteMenu> toSave = cleaned.stream()
                    .map(n -> FavoriteMenu.builder()
                            .name(n)
                            .group(group)
                            .build())
                    .toList();

            favoriteRepository.saveAll(toSave);
        }

        return group.getId();
    }

    @Transactional(readOnly = true)
    public List<FavoriteMenuDto> listFavoritesFlat(Long storeId) {
        // (선택) 스토어 존재 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        return favoriteRepository.findAllByStore(storeId).stream()
                .map(FavoriteMenuDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoriteMenuGroupedResponse listFavoritesGrouped(Long storeId) {

        // 1) 매장 존재 검증
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

        // 2) 그룹 + 메뉴 한번에 로드
        List<FavoriteMenuGroup> groups = groupRepository.findByStoreIdWithMenus(storeId);

        // 3) 매핑
        List<FavoriteMenuGroupBlock> blocks = groups.stream()
                .map(g -> FavoriteMenuGroupBlock.builder()
                        .groupId(g.getId())
                        .menu(g.getMenus().stream()
                                .map(FavoriteMenu::getName) // ★ 이름만 뽑기
                                .toList())
                        .build())
                .toList();

        // 4) 응답
        return FavoriteMenuGroupedResponse.builder()
                .groups(blocks)
                .build();
    }

}

