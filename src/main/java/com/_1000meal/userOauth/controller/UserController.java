package com._1000meal.userOauth.controller;

import com._1000meal.global.error.exception.CustomException;
import com._1000meal.global.response.ApiResponse;
import com._1000meal.global.error.code.UserLoginErrorCode;
import com._1000meal.userOauth.domain.User;
import com._1000meal.userOauth.dto.UpdateUserRequest;
import com._1000meal.userOauth.dto.UserDto;
import com._1000meal.userOauth.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            throw new CustomException(UserLoginErrorCode.USER_NOT_AUTHENTICATED);
        }

        return ApiResponse.ok(new UserDto(user));
    }

    @PatchMapping("/me")
    public ApiResponse<String> updateCurrentUser(@RequestBody UpdateUserRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            throw new CustomException(UserLoginErrorCode.USER_NOT_AUTHENTICATED);
        }

        User updatedUser = userService.updateUserInfo(user, request);
        session.setAttribute("user", updatedUser);

        return ApiResponse.ok("회원정보가 수정되었습니다.");
    }
}