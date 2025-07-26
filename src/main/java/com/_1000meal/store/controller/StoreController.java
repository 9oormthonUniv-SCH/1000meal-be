package com._1000meal.store.controller;

import com._1000meal.store.dto.StoreRequest;
import com._1000meal.store.dto.StoreResponse;
import com._1000meal.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public List<StoreResponse> getAllStores() {
        return storeService.getAllStores();
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(201).body(response);
    }
}