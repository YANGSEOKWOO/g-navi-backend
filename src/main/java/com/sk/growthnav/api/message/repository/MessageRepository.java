package com.sk.growthnav.api.message.repository;

import com.sk.growthnav.api.conversation.entity.Conversation;
import com.sk.growthnav.api.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationId(Long conversationId);

    void deleteByConversation(Conversation conversation);
}
