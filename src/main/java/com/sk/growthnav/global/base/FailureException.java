package com.sk.growthnav.global.base;

import com.sk.growthnav.global.apiPayload.code.base.BaseErrorCode;
import com.sk.growthnav.global.exception.GeneralException;

public class FailureException extends GeneralException {
    public FailureException(BaseErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
