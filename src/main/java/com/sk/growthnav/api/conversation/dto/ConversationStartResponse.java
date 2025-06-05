package com.sk.growthnav.api.conversation.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ConversationStartResponse {

    // MongoDB 에서 생성된 ID
    String conversationId;

    // AI가 생성한 첫 마디
    String botMessage;

    // 대화 시작 시간
    LocalDateTime timestamp;

    public static ConversationStartResponse of(String conversationId, String botMessage) {
        return ConversationStartResponse.builder()
                .conversationId(conversationId)
                .botMessage(botMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
