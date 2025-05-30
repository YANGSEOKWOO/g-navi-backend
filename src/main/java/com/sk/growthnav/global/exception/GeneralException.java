package com.sk.growthnav.global.exception;

import com.sk.growthnav.global.apiPayload.code.base.BaseErrorCode;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {
    private final BaseErrorCode baseErrorCode;

    public GeneralException(BaseErrorCode baseErrorCode) {
        this.baseErrorCode = baseErrorCode;
    }
}
