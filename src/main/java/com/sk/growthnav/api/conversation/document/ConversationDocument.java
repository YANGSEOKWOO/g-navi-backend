package com.sk.growthnav.api.conversation.document;

import com.sk.growthnav.api.conversation.entity.QuestionCategory;
import com.sk.growthnav.global.document.SenderType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
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
    // 추가: 대화의 주요 카테고리 (첫 번째 사용자 질문 기준)
    QuestionCategory primaryCategory;

    @Builder.Default
    List<MessageDocument> messages = new ArrayList<>();

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;

    // 메시지 추가 편의 메서드
    public void addMessage(SenderType senderType, String messageText) {
        MessageDocument newMessage = MessageDocument.builder()
                .senderType(senderType)
                .messageText(messageText)
                .timestamp(LocalDateTime.now())
                .build();

        // 사용자 메시지인 경우 카테고리 분석
        if (senderType == SenderType.USER) {
            QuestionCategory category = QuestionCategory.categorizeMessage(messageText);
            newMessage.setCategory(category);

            // 첫 번째 사용자 메시지인 경우 대화의 주요 카테고리로 설정
            if (this.primaryCategory == null) {
                this.primaryCategory = category;
            }
        }

        this.messages.add(newMessage);
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

    // 새로 추가: 오늘 대화했는지 확인
    public boolean isToday() {
        if (updatedAt == null) return false;
        return updatedAt.toLocalDate().equals(LocalDate.now());
    }

    // 새로 추가: 카테고리별 사용자 질문 수 조회
    public long getUserQuestionCountByCategory(QuestionCategory category) {
        return messages.stream()
                .filter(message -> message.getSenderType() == SenderType.USER)
                .filter(message -> category.equals(message.getCategory()))
                .count();
    }

    // 새로 추가: 전체 사용자 질문 수
    public long getTotalUserQuestions() {
        return getMessageCountBySenderType(SenderType.USER);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDocument {
        private SenderType senderType;
        private String messageText;
        private LocalDateTime timestamp;
        private QuestionCategory category;

        public String getSummary() {
            return String.format("%s: %s (at %s) [%s]",
                    senderType,
                    messageText.length() > 20 ? messageText.substring(0, 20) + "..." : messageText,
                    timestamp,
                    category);
        }
    }
}