package com.sk.growthnav.api.conversation.entity;

import com.sk.growthnav.api.message.entity.SenderType;
import com.sk.growthnav.global.base.BaseEntity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

// TODO::
@Document(collection = "chat_messages")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation extends BaseEntity {

    @Id
    String id;

    Long memberId;
    List<MessageDocument> messages;

    @Document
    public static class MessageDocument {
        private SenderType senderType;
        private String messageText;
        private LocalDateTime timestamp;
    }
}
