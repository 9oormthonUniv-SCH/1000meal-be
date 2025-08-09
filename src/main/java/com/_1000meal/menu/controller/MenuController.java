package com._1000meal.menu.controller;

import com._1000meal.menu.dto.WeeklyMenuRequest;
import com._1000meal.menu.dto.WeeklyMenuResponse;
import com._1000meal.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

//    @PostMapping("/weekly")
//    public ResponseEntity<WeeklyMenuResponse> createWeeklyMenu(@RequestBody WeeklyMenuRequest request) {
//        menuService.createWeeklyMenu(request);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
//    }

//    @GetMapping("/weekly/{storeId}")
//    public ResponseEntity<WeeklyMenuResponse> getWeeklyMenu(@PathVariable Long storeId) {
//        WeeklyMenuResponse response = menuService.getWeeklyMenu(storeId);
//        return ResponseEntity.ok(response);
//    }
}
