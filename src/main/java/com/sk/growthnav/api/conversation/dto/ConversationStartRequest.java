package com.sk.growthnav.api.conversation.dto;

// 클라이언트 => Spring

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationStartRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    Long memberId;

    // 기존 대화를 이어가고 싶을 때 (null이면 새 대화)
    String conversationId;
}
