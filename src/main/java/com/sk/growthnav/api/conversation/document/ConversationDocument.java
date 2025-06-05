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

// TODO::
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

    public void addMessage(SenderType senderType, String messageText) {
        this.messages.add(MessageDocument.builder()
                .senderType(senderType)
                .messageText(messageText)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDocument {
        private SenderType senderType;
        private String messageText;
        private LocalDateTime timestamp;
    }
}
