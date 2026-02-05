// com._1000meal.auth.service.EmailChangeServiceImpl
package com._1000meal.auth.service;

import com._1000meal.auth.dto.ChangeEmailConfirmRequest;
import com._1000meal.auth.dto.EmailChange.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailChangeServiceImpl implements EmailChangeService {

    private final AccountService accountService;

    private static final Duration TICKET_TTL = Duration.ofMinutes(10);
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

    @Override
    public EmailChangeStartResponse start(Long accountId, EmailChangeStartRequest req) {
        // 1) 현재 이메일 + 비밀번호 검증 (AccountService에 아래 메서드 추가 필요)
        accountService.verifyCredentialForEmailChange(accountId, req.currentEmail(), req.password());

        // 2) changeId 발급 및 저장
        String changeId = UUID.randomUUID().toString();
        tickets.put(changeId, Ticket.newVerified(accountId));

        return new EmailChangeStartResponse(changeId, TICKET_TTL.getSeconds());
    }

    @Override
    public void requestCode(Long accountId, EmailChangeRequestCodeRequest req) {
        Ticket t = getAlive(req.changeId())
                .filter(ticket -> Objects.equals(ticket.accountId, accountId))
                .orElseThrow(() -> new IllegalStateException("changeId가 유효하지 않거나 만료되었습니다."));

        // 비밀번호는 이미 start에서 검증 완료 → 여기서는 새 이메일로 코드만 발송
        accountService.requestChangeEmailCode(accountId, req.newEmail()); // ← AccountService에 신설

        t.newEmail = req.newEmail();
    }

    @Override
    public EmailChangeVerifyResponse verify(Long accountId, EmailChangeVerifyRequest req) {
        Ticket t = getAlive(req.changeId())
                .filter(ticket -> Objects.equals(ticket.accountId, accountId))
                .orElseThrow(() -> new IllegalStateException("changeId가 유효하지 않거나 만료되었습니다."));

        if (t.newEmail == null || t.newEmail.isBlank()) {
            throw new IllegalStateException("먼저 새 이메일로 인증 코드를 요청하세요.");
        }

        // 코드 검증 + 실제 이메일 변경 (기존 confirm 로직 재사용)
        accountService.confirmChangeEmail(
                accountId,
                new ChangeEmailConfirmRequest(t.newEmail, req.code())
        );

        tickets.remove(req.changeId());
        return new EmailChangeVerifyResponse(t.newEmail);
    }

    // ---- 내부 유틸 ----
    private Optional<Ticket> getAlive(String changeId) {
        Ticket t = tickets.get(changeId);
        if (t == null) return Optional.empty();
        if (t.isExpired()) {
            tickets.remove(changeId);
            return Optional.empty();
        }
        return Optional.of(t);
    }

    private static final class Ticket {
        final Long accountId;
        final Instant createdAt;
        String newEmail;

        private Ticket(Long accountId, Instant createdAt) {
            this.accountId = accountId;
            this.createdAt = createdAt;
        }

        static Ticket newVerified(Long accountId) {
            return new Ticket(accountId, Instant.now());
        }

        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plus(TICKET_TTL));
        }
    }
}