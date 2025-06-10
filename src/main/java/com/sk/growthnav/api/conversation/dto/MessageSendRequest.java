package com.sk.growthnav.api.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

// Spring => FastAPI (Message)

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageSendRequest {

    @NotNull(message = "대화 ID는 필수입니다.")
    String conversationId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    String messageText;

    @NotNull(message = "회원 ID는 필수입니다.")
    Long memberId;
}
