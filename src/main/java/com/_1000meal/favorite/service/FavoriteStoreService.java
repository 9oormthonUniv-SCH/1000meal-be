package com._1000meal.favorite.service;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.favorite.domain.FavoriteStore;
import com._1000meal.favorite.repository.FavoriteStoreRepository;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteStoreService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final AccountRepository accountRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void addFavorite(Long accountId, Long storeId) {
        // 빠른 멱등 처리
        if (favoriteStoreRepository.existsByAccountIdAndStoreId(accountId, storeId)) {
            return;
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("account not found: " + accountId));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("store not found: " + storeId));

        try {
            favoriteStoreRepository.save(FavoriteStore.of(account, store));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청 등으로 유니크 제약 걸린 경우 -> 멱등 처리
            // (이미 즐겨찾기 상태로 간주)
        }
    }

    @Transactional
    public void removeFavorite(Long accountId, Long storeId) {
        // deleteBy...는 존재하면 1, 없으면 0 반환
        favoriteStoreRepository.deleteByAccountIdAndStoreId(accountId, storeId);
    }

    public List<FavoriteStore> getMyFavorites(Long accountId) {
        return favoriteStoreRepository.findAllByAccountId(accountId);
    }
}