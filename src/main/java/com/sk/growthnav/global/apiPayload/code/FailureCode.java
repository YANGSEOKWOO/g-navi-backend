package com.sk.growthnav.global.apiPayload.code;

import com.sk.growthnav.global.apiPayload.code.base.BaseErrorCode;
import com.sk.growthnav.global.apiPayload.dto.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum FailureCode implements BaseErrorCode {

    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증에 실패했습니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "권한이 없습니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "컨텐츠를 찾지 못했습니다."),
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 관리자에게 문의하세요"),

    // Member Error Codes
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "회원을 찾을 수 없습니다."),
    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER409", "이미 존재하는 이메일입니다."),
    MEMBER_INVALID_EMAIL(HttpStatus.BAD_REQUEST, "MEMBER400_1", "올바르지 않은 이메일 형식입니다."),
    MEMBER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER400_2", "비밀번호는 8자 이상이어야 합니다.");


    private final HttpStatus httpStatus;

    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .httpStatus(this.httpStatus)
                .isSuccess(false)
                .code(this.code)
                .message(this.message)
                .build();
    }
}
