package com._1000meal.favoriteMenu.service;

import com._1000meal.favoriteMenu.domain.FavoriteMenu;
import com._1000meal.favoriteMenu.domain.FavoriteMenuGroup;
import com._1000meal.favoriteMenu.dto.FavoriteMenuGroupedResponse;
import com._1000meal.favoriteMenu.repository.FavoriteMenuGroupRepository;
import com._1000meal.favoriteMenu.repository.FavoriteMenuRepository;
import com._1000meal.global.error.code.MenuErrorCode;
import com._1000meal.global.error.code.StoreErrorCode;
import com._1000meal.global.error.exception.CustomException;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteMenuServiceTest {

    @Mock StoreRepository storeRepository;
    @Mock FavoriteMenuGroupRepository groupRepository;
    @Mock FavoriteMenuRepository favoriteRepository;

    @InjectMocks FavoriteMenuService service;

    // -----------------------------
    // createGroupAndReplaceFavorites
    // -----------------------------

    @Test
    @DisplayName("createGroupAndReplaceFavorites: 매장 없으면 STORE_NOT_FOUND")
    void createGroupAndReplaceFavorites_storeNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.createGroupAndReplaceFavorites(1L, List.of("김치찌개")));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
        verify(storeRepository).findById(1L);
        verifyNoMoreInteractions(storeRepository);
        verifyNoInteractions(groupRepository, favoriteRepository);
    }

    @Test
    @DisplayName("createGroupAndReplaceFavorites: names가 null/공백이면 그룹만 생성하고 favorites 저장 안 함")
    void createGroupAndReplaceFavorites_namesEmpty_onlyCreateGroup() {
        Store store = mock(Store.class);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        FavoriteMenuGroup savedGroup = mock(FavoriteMenuGroup.class);
        when(savedGroup.getId()).thenReturn(100L);
        when(groupRepository.save(any(FavoriteMenuGroup.class))).thenReturn(savedGroup);

        Long groupId = service.createGroupAndReplaceFavorites(1L, List.of("  ", null, ""));

        assertEquals(100L, groupId);

        verify(storeRepository).findById(1L);
        verify(groupRepository).save(any(FavoriteMenuGroup.class));
        verifyNoInteractions(favoriteRepository); // cleaned 비어있으면 delete/saveAll도 호출 안 함
        verifyNoMoreInteractions(storeRepository, groupRepository);
    }

    @Test
    @DisplayName("createGroupAndReplaceFavorites: 이름 정제(trim/empty 제거/distinct) 후 delete + saveAll 수행")
    void createGroupAndReplaceFavorites_cleanAndSave() {
        Store store = mock(Store.class);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        FavoriteMenuGroup savedGroup = mock(FavoriteMenuGroup.class);
        when(savedGroup.getId()).thenReturn(100L);
        when(groupRepository.save(any(FavoriteMenuGroup.class))).thenReturn(savedGroup);

        // when
        Long groupId = service.createGroupAndReplaceFavorites(
                1L,
                List.of(" 김치찌개 ", "김치찌개", "  ", null, "된장찌개")
        );

        // then
        assertEquals(100L, groupId);

        verify(storeRepository).findById(1L);
        verify(groupRepository).save(any(FavoriteMenuGroup.class));
        verify(favoriteRepository).deleteByGroup_Id(100L);

        ArgumentCaptor<List<FavoriteMenu>> captor = ArgumentCaptor.forClass(List.class);
        verify(favoriteRepository).saveAll(captor.capture());

        List<FavoriteMenu> saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(2, saved.size()); // 김치찌개, 된장찌개

        // 엔티티 getter가 없다면 여기까지만 검증해도 충분.
        // getter가 있으면 아래 추가 검증 가능:
        // assertTrue(saved.stream().anyMatch(m -> "김치찌개".equals(m.getName())));
        // assertTrue(saved.stream().anyMatch(m -> "된장찌개".equals(m.getName())));

        verifyNoMoreInteractions(storeRepository, groupRepository, favoriteRepository);
    }

    // -----------------------------
    // getAllFavoritesGrouped
    // -----------------------------

    @Test
    @DisplayName("getAllFavoritesGrouped: 매장 없으면 STORE_NOT_FOUND")
    void getAllFavoritesGrouped_storeNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.getAllFavoritesGrouped(1L));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());
        verify(storeRepository).findById(1L);
        verifyNoMoreInteractions(storeRepository);
        verifyNoInteractions(groupRepository, favoriteRepository);
    }

    @Test
    @DisplayName("getAllFavoritesGrouped: store 검증 후 groupRepository.findByStoreIdWithMenus 호출, 응답 groups size 매핑")
    void getAllFavoritesGrouped_success_maps() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(mock(Store.class)));

        FavoriteMenuGroup g1 = mock(FavoriteMenuGroup.class);
        when(g1.getId()).thenReturn(10L);
        when(g1.getMenus()).thenReturn(List.of(
                mockFavorite("김치찌개"),
                mockFavorite("된장찌개")
        ));

        FavoriteMenuGroup g2 = mock(FavoriteMenuGroup.class);
        when(g2.getId()).thenReturn(20L);
        when(g2.getMenus()).thenReturn(List.of(
                mockFavorite("돈까스")
        ));

        when(groupRepository.findByStoreIdWithMenus(1L)).thenReturn(List.of(g1, g2));

        FavoriteMenuGroupedResponse resp = service.getAllFavoritesGrouped(1L);

        assertNotNull(resp);
        assertNotNull(resp.getGroups());
        assertEquals(2, resp.getGroups().size());
        assertEquals(10L, resp.getGroups().get(0).getGroupId());
        assertEquals(List.of("김치찌개", "된장찌개"), resp.getGroups().get(0).getMenu());
        assertEquals(20L, resp.getGroups().get(1).getGroupId());
        assertEquals(List.of("돈까스"), resp.getGroups().get(1).getMenu());

        verify(storeRepository).findById(1L);
        verify(groupRepository).findByStoreIdWithMenus(1L);
        verifyNoMoreInteractions(storeRepository, groupRepository);
        verifyNoInteractions(favoriteRepository);
    }

    // -----------------------------
    // getFavoritesGroupedByGroup
    // -----------------------------

    @Test
    @DisplayName("getFavoritesGroupedByGroup: 그룹 없으면 FAVORITE_GROUP_NOT_FOUND")
    void getFavoritesGroupedByGroup_groupNotFound() {
        when(groupRepository.findById(10L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.getFavoritesGroupedByGroup(10L));

        assertEquals(MenuErrorCode.FAVORITE_GROUP_NOT_FOUND, ex.getErrorCodeIfs());
        verify(groupRepository).findById(10L);
        verifyNoMoreInteractions(groupRepository);
        verifyNoInteractions(storeRepository, favoriteRepository);
    }

    @Test
    @DisplayName("getFavoritesGroupedByGroup: 그룹 존재하면 favorites 조회하여 단일 블록으로 반환")
    void getFavoritesGroupedByGroup_success() {
        when(groupRepository.findById(10L)).thenReturn(Optional.of(mock(FavoriteMenuGroup.class)));

        when(favoriteRepository.findByGroup_IdOrderByIdAsc(10L)).thenReturn(List.of(
                mockFavorite("김치찌개"),
                mockFavorite("된장찌개")
        ));

        FavoriteMenuGroupedResponse resp = service.getFavoritesGroupedByGroup(10L);

        assertNotNull(resp);
        assertNotNull(resp.getGroups());
        assertEquals(1, resp.getGroups().size());
        assertEquals(10L, resp.getGroups().get(0).getGroupId());
        assertEquals(List.of("김치찌개", "된장찌개"), resp.getGroups().get(0).getMenu());

        verify(groupRepository).findById(10L);
        verify(favoriteRepository).findByGroup_IdOrderByIdAsc(10L);
        verifyNoMoreInteractions(groupRepository, favoriteRepository);
        verifyNoInteractions(storeRepository);
    }

    // -----------------------------
    // replaceFavoritesInGroup
    // -----------------------------

    @Test
    @DisplayName("replaceFavoritesInGroup: 그룹 없으면 FAVORITE_GROUP_NOT_FOUND")
    void replaceFavoritesInGroup_groupNotFound() {
        when(groupRepository.findById(10L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.replaceFavoritesInGroup(10L, List.of("김치찌개")));

        assertEquals(MenuErrorCode.FAVORITE_GROUP_NOT_FOUND, ex.getErrorCodeIfs());
        verify(groupRepository).findById(10L);
        verifyNoMoreInteractions(groupRepository);
        verifyNoInteractions(storeRepository, favoriteRepository);
    }

    @Test
    @DisplayName("replaceFavoritesInGroup: cleaned 비어있으면 delete만 하고 saveAll은 안 함")
    void replaceFavoritesInGroup_emptyOnlyDelete() {
        FavoriteMenuGroup group = mock(FavoriteMenuGroup.class);
        when(group.getId()).thenReturn(10L);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        service.replaceFavoritesInGroup(10L, List.of(" ", null, ""));

        verify(groupRepository).findById(10L);
        verify(favoriteRepository).deleteByGroup_Id(10L);
        verifyNoMoreInteractions(groupRepository, favoriteRepository);
        verifyNoInteractions(storeRepository);
    }

    @Test
    @DisplayName("replaceFavoritesInGroup: cleaned 있으면 delete 후 saveAll")
    void replaceFavoritesInGroup_successDeleteThenSaveAll() {
        FavoriteMenuGroup group = mock(FavoriteMenuGroup.class);
        when(group.getId()).thenReturn(10L);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

        service.replaceFavoritesInGroup(10L, List.of(" 김치찌개 ", "김치찌개", "된장찌개"));

        verify(groupRepository).findById(10L);
        verify(favoriteRepository).deleteByGroup_Id(10L);

        ArgumentCaptor<List<FavoriteMenu>> captor = ArgumentCaptor.forClass(List.class);
        verify(favoriteRepository).saveAll(captor.capture());

        assertEquals(2, captor.getValue().size());
        verifyNoMoreInteractions(groupRepository, favoriteRepository);
        verifyNoInteractions(storeRepository);
    }

    // -----------------------------
    // deleteGroups
    // -----------------------------

    @Test
    @DisplayName("deleteGroups: store 없으면 STORE_NOT_FOUND")
    void deleteGroups_storeNotFound() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> service.deleteGroups(1L, List.of(10L)));

        assertEquals(StoreErrorCode.STORE_NOT_FOUND, ex.getErrorCodeIfs());

        verify(storeRepository).findById(1L);
        verifyNoMoreInteractions(storeRepository);
        verifyNoInteractions(groupRepository, favoriteRepository);
    }

    @Test
    @DisplayName("deleteGroups: groupIds null/비어있으면 FAVORITE_GROUP_NOT_FOUND")
    void deleteGroups_emptyIds_throws() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(mock(Store.class)));

        CustomException ex1 = assertThrows(CustomException.class,
                () -> service.deleteGroups(1L, null));
        assertEquals(MenuErrorCode.FAVORITE_GROUP_NOT_FOUND, ex1.getErrorCodeIfs());

        CustomException ex2 = assertThrows(CustomException.class,
                () -> service.deleteGroups(1L, List.of()));
        assertEquals(MenuErrorCode.FAVORITE_GROUP_NOT_FOUND, ex2.getErrorCodeIfs());

        verify(storeRepository, times(2)).findById(1L);
        verifyNoMoreInteractions(storeRepository);
        verifyNoInteractions(groupRepository, favoriteRepository);
    }

    @Test
    @DisplayName("deleteGroups: ownedIds 비어있으면 아무것도 삭제 안 함")
    void deleteGroups_ownedEmpty_noDelete() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(mock(Store.class)));
        when(groupRepository.findOwnedIds(eq(1L), anyList())).thenReturn(List.of());

        service.deleteGroups(1L, List.of(10L, 20L));

        verify(storeRepository).findById(1L);
        verify(groupRepository).findOwnedIds(eq(1L), anyList());
        verifyNoMoreInteractions(storeRepository, groupRepository);
        verifyNoInteractions(favoriteRepository);
    }

    @Test
    @DisplayName("deleteGroups: ownedIds 있으면 favorites 먼저 삭제 후 groups 삭제")
    void deleteGroups_success_deletesInOrder() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(mock(Store.class)));
        when(groupRepository.findOwnedIds(eq(1L), anyList())).thenReturn(List.of(10L, 20L));

        service.deleteGroups(1L, List.of(10L, 20L, 20L, null));

        // 순서 검증 (FK 안전: 메뉴 -> 그룹)
        var inOrder = inOrder(favoriteRepository, groupRepository);
        inOrder.verify(favoriteRepository).deleteByGroup_IdIn(List.of(10L, 20L));
        inOrder.verify(groupRepository).deleteAllById(List.of(10L, 20L));

        verify(storeRepository).findById(1L);
        verify(groupRepository).findOwnedIds(eq(1L), anyList());
        verifyNoMoreInteractions(storeRepository, groupRepository, favoriteRepository);
    }

    // -----------------------------
    // helpers
    // -----------------------------

    private FavoriteMenu mockFavorite(String name) {
        FavoriteMenu fm = mock(FavoriteMenu.class);
        when(fm.getName()).thenReturn(name);
        return fm;
    }
}