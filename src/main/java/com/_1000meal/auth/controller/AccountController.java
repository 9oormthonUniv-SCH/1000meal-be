package com._1000meal.auth.controller;

import com._1000meal.auth.dto.*;
import com._1000meal.auth.model.AuthPrincipal;
import com._1000meal.auth.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Account",
        description = "로그인 이후 계정 관리 기능 (아이디 찾기, 비밀번호 변경, 회원 탈퇴, 이메일 변경)"
)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "아이디(학번) 찾기",
            description = """
                    가입된 계정의 사용자 식별자(예: 학번/아이디)를 조회합니다.
                    
                    - 사용자가 입력한 정보로 계정을 조회해 아이디를 반환합니다.
                    - 실패 시(일치 계정 없음 등) 에러 응답을 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류/일치 계정 없음 등",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/find-id")
    public ResponseEntity<FindIdResponse> findId(@RequestBody @Valid FindIdRequest req) {
        return ResponseEntity.ok(accountService.findId(req));
    }

    @Operation(
            summary = "비밀번호 변경(로그인 필요)",
            description = """
                    로그인된 사용자가 비밀번호를 변경합니다.
                    
                    - 현재 비밀번호 검증 후 새 비밀번호로 변경합니다.
                    - 새 비밀번호가 정책에 맞지 않거나, 기존 비밀번호와 동일한 경우 실패할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 정책 위반/기존 비밀번호 불일치/동일 비밀번호 등",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패(로그인 필요)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid ChangePasswordRequest req
    ) {
        accountService.changePasswordByAccountId(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    @Operation(
            summary = "회원 탈퇴(로그인 필요)",
            description = """
                    로그인된 사용자가 회원 탈퇴를 진행합니다.
                    
                    - 소프트 삭제(soft delete) 방식으로 계정 상태를 변경합니다.
                    - 이후 동일 식별자로 재가입 정책은 서비스 규칙에 따릅니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패(로그인 필요)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal
    ) {
        accountService.deleteOwnAccountByAccountId(principal.id());
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }

    @Operation(
            summary = "이메일 변경 인증코드 발송(로그인 필요)",
            description = """
                    새 이메일로 변경하기 위한 인증 코드를 발송합니다.
                    
                    - 학교 이메일 도메인(@sch.ac.kr)만 허용됩니다.
                    - 너무 잦은 요청은 제한될 수 있습니다(레이트 리밋/쿨다운).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            @ApiResponse(responseCode = "400", description = "학교 도메인 아님/요청값 오류 등",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패(로그인 필요)",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "요청 제한(너무 잦은 요청)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/email/change/request")
    public ResponseEntity<Map<String, String>> requestChangeEmail(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid ChangeEmailRequest req
    ) {
        accountService.requestChangeEmail(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 새 이메일로 전송되었습니다."));
    }

    @Operation(
            summary = "이메일 변경 확정(인증코드 확인, 로그인 필요)",
            description = """
                    발송된 인증 코드를 검증한 뒤 이메일 변경을 확정합니다.
                    
                    - 인증 코드가 유효해야 합니다(만료/불일치 시 실패).
                    - 성공 시 계정 이메일이 새 이메일로 변경됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 변경 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치/만료/요청값 오류 등",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증 실패(로그인 필요)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/email/change/confirm")
    public ResponseEntity<Map<String, String>> confirmChangeEmail(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid ChangeEmailConfirmRequest req
    ) {
        accountService.confirmChangeEmail(principal.id(), req);
        return ResponseEntity.ok(Map.of("message", "이메일이 변경되었습니다."));
    }
}