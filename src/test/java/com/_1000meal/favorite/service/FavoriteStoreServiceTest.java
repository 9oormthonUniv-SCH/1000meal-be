package com._1000meal.favorite.service;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.favorite.domain.FavoriteStore;
import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteStoreServiceTest {

    @Mock
    FavoriteStoreRepository favoriteStoreRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    FavoriteStoreService favoriteStoreService;

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite {

        @Test
        @DisplayName("성공: 새로운 즐겨찾기 추가")
        void addFavorite_success() {
            // given
            Long accountId = 1L;
            Long storeId = 10L;

            Account account = mock(Account.class);
            Store store = mock(Store.class);

            when(favoriteStoreRepository.existsByAccountIdAndStoreId(accountId, storeId)).thenReturn(false);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            // when
            favoriteStoreService.addFavorite(accountId, storeId);

            // then
            verify(favoriteStoreRepository).existsByAccountIdAndStoreId(accountId, storeId);
            verify(accountRepository).findById(accountId);
            verify(storeRepository).findById(storeId);
            verify(favoriteStoreRepository).save(any(FavoriteStore.class));
        }

        @Test
        @DisplayName("멱등성: 이미 즐겨찾기된 경우 저장하지 않음")
        void addFavorite_alreadyExists_doNothing() {
            // given
            Long accountId = 1L;
            Long storeId = 10L;

            when(favoriteStoreRepository.existsByAccountIdAndStoreId(accountId, storeId)).thenReturn(true);

            // when
            favoriteStoreService.addFavorite(accountId, storeId);

            // then
            verify(favoriteStoreRepository).existsByAccountIdAndStoreId(accountId, storeId);
            verify(accountRepository, never()).findById(any());
            verify(storeRepository, never()).findById(any());
            verify(favoriteStoreRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 계정")
        void addFavorite_accountNotFound_throwsException() {
            // given
            Long accountId = 999L;
            Long storeId = 10L;

            when(favoriteStoreRepository.existsByAccountIdAndStoreId(accountId, storeId)).thenReturn(false);
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> favoriteStoreService.addFavorite(accountId, storeId)
            );

            assertTrue(exception.getMessage().contains("account not found"));
            verify(storeRepository, never()).findById(any());
            verify(favoriteStoreRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 매장")
        void addFavorite_storeNotFound_throwsException() {
            // given
            Long accountId = 1L;
            Long storeId = 999L;

            Account account = mock(Account.class);

            when(favoriteStoreRepository.existsByAccountIdAndStoreId(accountId, storeId)).thenReturn(false);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> favoriteStoreService.addFavorite(accountId, storeId)
            );

            assertTrue(exception.getMessage().contains("store not found"));
            verify(favoriteStoreRepository, never()).save(any());
        }

        @Test
        @DisplayName("멱등성: 동시 요청으로 DataIntegrityViolationException 발생 시 무시")
        void addFavorite_concurrentRequest_handledGracefully() {
            // given
            Long accountId = 1L;
            Long storeId = 10L;

            Account account = mock(Account.class);
            Store store = mock(Store.class);

            when(favoriteStoreRepository.existsByAccountIdAndStoreId(accountId, storeId)).thenReturn(false);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(favoriteStoreRepository.save(any(FavoriteStore.class)))
                    .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

            // when & then (예외 발생하지 않음)
            assertDoesNotThrow(() -> favoriteStoreService.addFavorite(accountId, storeId));

            verify(favoriteStoreRepository).save(any(FavoriteStore.class));
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite {

        @Test
        @DisplayName("성공: 즐겨찾기 삭제")
        void removeFavorite_success() {
            // given
            Long accountId = 1L;
            Long storeId = 10L;

            when(favoriteStoreRepository.deleteByAccountIdAndStoreId(accountId, storeId)).thenReturn(1L);

            // when
            favoriteStoreService.removeFavorite(accountId, storeId);

            // then
            verify(favoriteStoreRepository).deleteByAccountIdAndStoreId(accountId, storeId);
        }

        @Test
        @DisplayName("멱등성: 존재하지 않는 즐겨찾기 삭제 시도해도 예외 없음")
        void removeFavorite_notExists_noException() {
            // given
            Long accountId = 1L;
            Long storeId = 999L;

            when(favoriteStoreRepository.deleteByAccountIdAndStoreId(accountId, storeId)).thenReturn(0L);

            // when & then (예외 발생하지 않음)
            assertDoesNotThrow(() -> favoriteStoreService.removeFavorite(accountId, storeId));

            verify(favoriteStoreRepository).deleteByAccountIdAndStoreId(accountId, storeId);
        }
    }

    @Nested
    @DisplayName("getMyFavorites")
    class GetMyFavorites {

        @Test
        @DisplayName("성공: 즐겨찾기 목록 반환")
        void getMyFavorites_success() {
            // given
            Long accountId = 1L;

            FavoriteStore favorite1 = mock(FavoriteStore.class);
            FavoriteStore favorite2 = mock(FavoriteStore.class);
            List<FavoriteStore> favorites = List.of(favorite1, favorite2);

            when(favoriteStoreRepository.findAllByAccountId(accountId)).thenReturn(favorites);

            // when
            List<FavoriteStore> result = favoriteStoreService.getMyFavorites(accountId);

            // then
            assertEquals(2, result.size());
            verify(favoriteStoreRepository).findAllByAccountId(accountId);
        }

        @Test
        @DisplayName("성공: 즐겨찾기가 없으면 빈 목록 반환")
        void getMyFavorites_empty() {
            // given
            Long accountId = 1L;

            when(favoriteStoreRepository.findAllByAccountId(accountId)).thenReturn(Collections.emptyList());

            // when
            List<FavoriteStore> result = favoriteStoreService.getMyFavorites(accountId);

            // then
            assertTrue(result.isEmpty());
            verify(favoriteStoreRepository).findAllByAccountId(accountId);
        }
    }
}
