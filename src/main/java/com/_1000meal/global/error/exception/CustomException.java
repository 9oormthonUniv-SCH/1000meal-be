package com._1000meal.global.error.exception;

import com._1000meal.global.error.code.ErrorCode;
import lombok.Getter;


@Getter
public class CustomException extends RuntimeException {

    private final ErrorCodeIfs errorCodeIfs;
    private final String customMessage;   // 사용자 정의 메시지 (없으면 기본 메시지)

    public CustomException(ErrorCodeIfs errorCodeIfs) {
        super(errorCodeIfs.getMessage());
        this.errorCodeIfs = errorCodeIfs;
        this.customMessage = null;
    }

    public CustomException(ErrorCodeIfs errorCodeIfs, Throwable cause) {
        super(errorCodeIfs.getMessage(), cause);
        this.errorCodeIfs = errorCodeIfs;
        this.customMessage = null;
    }

    // ✅ 사용자 정의 메시지 포함 생성자
    public CustomException(ErrorCodeIfs errorCodeIfs, String customMessage) {
        super(customMessage == null || customMessage.isBlank()
                ? errorCodeIfs.getMessage()
                : customMessage);
        this.errorCodeIfs = errorCodeIfs;
        this.customMessage = customMessage;
    }

    /** 응답용 메시지 */
    public String getDisplayMessage() {
        return (customMessage == null || customMessage.isBlank())
                ? errorCodeIfs.getMessage()
                : customMessage;
    }




}