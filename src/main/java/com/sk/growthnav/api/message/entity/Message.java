package com.sk.growthnav.api.message.entity;

import com.sk.growthnav.api.conversation.entity.Conversation;
import com.sk.growthnav.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Message extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", unique = true, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    SenderType sendertype;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    String messageText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    Conversation conversation;
}
