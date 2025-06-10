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
    MEMBER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER400_2", "비밀번호는 8자 이상이어야 합니다."),
    
    // Member 관련 추가 에러 코드들
    MEMBER_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MEMBER401", "이메일 또는 비밀번호가 일치하지 않습니다."),
    MEMBER_INVALID_NAME(HttpStatus.BAD_REQUEST, "MEMBER400_3", "이름은 1자 이상 15자 이하여야 합니다."),

    // Conversation 관련 에러 코드들 (나중에 사용)
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONVERSATION404", "대화를 찾을 수 없습니다."),
    CONVERSATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CONVERSATION403", "해당 대화에 접근할 권한이 없습니다."),
    CONVERSATION_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "CONVERSATION400", "메시지 내용이 비어있습니다."),

    // FastAPI 통신 관련 에러 코드들 (나중에 사용)
    FASTAPI_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "FASTAPI503", "AI 서비스에 연결할 수 없습니다."),
    FASTAPI_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "FASTAPI408", "AI 서비스 응답 시간이 초과되었습니다.");


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
