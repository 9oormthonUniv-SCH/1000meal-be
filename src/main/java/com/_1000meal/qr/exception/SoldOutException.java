package com._1000meal.qr.exception;

import com._1000meal.global.error.code.ErrorCode;
import com._1000meal.global.error.exception.CustomException;

public class SoldOutException extends CustomException {

    public SoldOutException() {
        super(ErrorCode.CONFLICT, "오늘 수량이 마감되었습니다.");
    }
}
