package com.sk.growthnav.api.conversation.document;

import com.sk.growthnav.global.document.SenderType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chat_messages")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ConversationDocument {

    @Id
    String id;

    Long memberId;

    @Builder.Default
    List<MessageDocument> messages = new ArrayList<>();

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    // 메시지 추가 편의 메서드
    public void addMessage(SenderType senderType, String messageText) {
        this.messages.add(MessageDocument.builder()
                .senderType(senderType)
                .messageText(messageText)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ConversationService에서 필요한 메서드들 추가

    // 최신 메시지 조회
    public MessageDocument getLatestMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    // 메시지 개수 조회
    public int getMessageCount() {
        return messages.size();
    }

    // 특정 타입의 메시지 개수 조회
    public long getMessageCountBySenderType(SenderType senderType) {
        return messages.stream()
                .filter(message -> message.getSenderType() == senderType)
                .count();
    }

    // 대화가 비어있는지 확인
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    // 마지막 사용자 메시지 조회 (FastAPI 컨텍스트용)
    public MessageDocument getLastUserMessage() {
        return messages.stream()
                .filter(message -> message.getSenderType() == SenderType.USER)
                .reduce((first, second) -> second)  // 마지막 요소 반환
                .orElse(null);
    }

    // 대화 요약 정보 (디버깅/로깅용)
    public String getSummary() {
        return String.format("Conversation[id=%s, memberId=%s, messageCount=%d]",
                id, memberId, getMessageCount());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDocument {
        private SenderType senderType;
        private String messageText;
        private LocalDateTime timestamp;

        // 메시지 요약 (디버깅용)
        public String getSummary() {
            return String.format("%s: %s (at %s)",
                    senderType,
                    messageText.length() > 20 ? messageText.substring(0, 20) + "..." : messageText,
                    timestamp);
        }
    }
}