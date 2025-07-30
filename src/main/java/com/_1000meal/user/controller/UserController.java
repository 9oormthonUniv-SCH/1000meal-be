package com._1000meal.user.controller;

import com._1000meal.user.domain.User;
import com._1000meal.user.dto.UpdateUserRequest;
import com._1000meal.user.dto.UserDto;
import com._1000meal.user.repository.UserRepository;
import com._1000meal.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        return ResponseEntity.ok(new UserDto(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody UpdateUserRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        User updatedUser = userService.updateUserInfo(user, request); // 서비스 호출
        session.setAttribute("user", updatedUser);

        return ResponseEntity.ok("회원정보가 수정되었습니다.");
    }
}