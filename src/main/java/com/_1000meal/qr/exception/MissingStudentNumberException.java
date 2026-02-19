package com._1000meal.qr.exception;

import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;

public class MissingStudentNumberException extends CustomException {

    public MissingStudentNumberException() {
        super(ErrorCode.UNPROCESSABLE_ENTITY, "학번 정보가 없어 이용할 수 없습니다.");
    }
}
