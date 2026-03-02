package com._1000meal.favorite.repository;

import com._1000meal.auth.model.Account;
import com._1000meal.auth.model.AccountStatus;
import com._1000meal.auth.repository.AccountRepository;
import com._1000meal.favorite.domain.FavoriteStore;
import com._1000meal.favorite.dto.FavoriteStoreResponse;
import com._1000meal.global.constant.Role;
import com._1000meal.store.domain.Store;
import com._1000meal.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class FavoriteStoreRepositoryTest {

    @Autowired
    FavoriteStoreRepository favoriteStoreRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    StoreRepository storeRepository;

    @Test
    @DisplayName("즐겨찾기 조회 projection: store 정보가 DTO로 반환된다")
    void findFavoriteStores_returnsProjection() {
        Account account = accountRepository.save(new Account(
                null,
                "user01",
                "user01@sch.ac.kr",
                "hash",
                Role.STUDENT,
                AccountStatus.ACTIVE
        ));

        Store store = storeRepository.save(Store.builder()
                .name("store")
                .address("addr")
                .phone("010-0000-0000")
                .description("desc")
                .isOpen(true)
                .remain(10)
                .hours("08:00 ~ 소진 시")
                .lat(0.0)
                .lng(0.0)
                .imageUrl("img")
                .build());

        favoriteStoreRepository.save(FavoriteStore.of(account, store));

        List<FavoriteStoreResponse> result = favoriteStoreRepository.findFavoriteStores(account.getId());

        assertEquals(1, result.size());
        FavoriteStoreResponse dto = result.get(0);
        assertEquals(store.getId(), dto.storeId());
        assertEquals("store", dto.storeName());
        assertEquals("img", dto.storeImageUrl());
        assertTrue(dto.storeIsOpen());
    }
}
