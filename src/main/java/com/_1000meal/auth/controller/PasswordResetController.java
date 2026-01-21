package com._1000meal.auth.controller;

import com._1000meal.auth.dto.PasswordResetConfirmRequest;
import com._1000meal.auth.dto.PasswordResetRequest;
import com._1000meal.auth.dto.SimpleMessageResponse;
import com._1000meal.auth.service.PasswordResetService;
import com._1000meal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Auth - Password",
        description = "비로그인 사용자를 위한 비밀번호 재설정 API (요청 → 확인)"
)
@RestController
@RequestMapping("/api/v1/auth/password/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "비밀번호 재설정 요청",
            description = """
                    비밀번호 재설정을 위한 이메일 요청 API입니다. (비로그인)
                    
                    - 가입된 이메일로 비밀번호 재설정 링크 또는 인증 코드를 발송합니다.
                    - 보안상, 이메일 존재 여부와 관계없이 동일한 응답 메시지를 반환합니다.
                    - 일정 시간 내 반복 요청 시 제한될 수 있습니다.
                    """
    )
    @PostMapping("/request")
    public ApiResponse<SimpleMessageResponse> request(
            @RequestBody @Valid PasswordResetRequest req
    ) {
        passwordResetService.requestReset(req);
        return ApiResponse.ok(
                new SimpleMessageResponse("재설정 링크를 이메일로 보냈습니다.")
        );
    }

    @Operation(
            summary = "비밀번호 재설정 확정",
            description = """
                    비밀번호 재설정 요청 후,
                    전달받은 토큰(또는 인증 코드)을 이용해 새 비밀번호를 설정합니다. (비로그인)
                    
                    - 토큰이 유효하지 않거나 만료된 경우 실패합니다.
                    - 기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.
                    """
    )
    @PostMapping("/confirm")
    public ApiResponse<SimpleMessageResponse> confirm(
            @RequestBody @Valid PasswordResetConfirmRequest req
    ) {
        passwordResetService.confirmReset(req);
        return ApiResponse.ok(
                new SimpleMessageResponse("비밀번호가 재설정되었습니다.")
        );
    }
}