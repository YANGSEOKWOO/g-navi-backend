package com.sk.growthnav.api.conversation.repository;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.entity.QuestionCategory;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ConversationRepository extends MongoRepository<ConversationDocument, String> {

    // 기존 메서드들
    List<ConversationDocument> findByMemberId(Long memberId);

    List<ConversationDocument> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // ===== 대시보드용 새로운 메서드들 =====

    /**
     * 오늘 대화한 고유 사용자 수 조회
     */
    @Query("{'updatedAt': {$gte: ?0, $lt: ?1}}")
    List<ConversationDocument> findConversationsUpdatedBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * 특정 기간 동안 대화한 고유 사용자 ID 조회
     */
    @Aggregation(pipeline = {
            "{ $match: { 'updatedAt': { $gte: ?0, $lt: ?1 } } }",
            "{ $group: { _id: '$memberId' } }",
            "{ $count: 'uniqueUsers' }"
    })
    Long countUniqueUsersUpdatedBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * 주요 카테고리별 대화 수 조회
     */
    @Aggregation(pipeline = {
            "{ $group: { _id: '$primaryCategory', count: { $sum: 1 } } }"
    })
    List<CategoryCountResult> countByPrimaryCategory();

    /**
     * 특정 회원의 카테고리별 질문 수 조회 (메시지 단위)
     */
    @Aggregation(pipeline = {
            "{ $match: { 'memberId': ?0 } }",
            "{ $unwind: '$messages' }",
            "{ $match: { 'messages.senderType': 'USER' } }",
            "{ $group: { _id: '$messages.category', count: { $sum: 1 } } }"
    })
    List<CategoryCountResult> countUserQuestionsByMemberAndCategory(Long memberId);

    /**
     * 전체 사용자의 카테고리별 질문 수 조회 (메시지 단위)
     */
    @Aggregation(pipeline = {
            "{ $unwind: '$messages' }",
            "{ $match: { 'messages.senderType': 'USER' } }",
            "{ $group: { _id: '$messages.category', count: { $sum: 1 } } }"
    })
    List<CategoryCountResult> countAllUserQuestionsByCategory();

    /**
     * 특정 등급 사용자들의 카테고리별 질문 수 조회
     * (이건 Member 정보와 조합해서 서비스에서 처리)
     */
    @Query("{'memberId': {$in: ?0}}")
    List<ConversationDocument> findByMemberIdIn(List<Long> memberIds);

    /**
     * 특정 날짜 범위의 대화 조회
     */
    @Query("{'createdAt': {$gte: ?0, $lt: ?1}}")
    List<ConversationDocument> findConversationsCreatedBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 카테고리 집계 결과를 받기 위한 인터페이스
     */
    interface CategoryCountResult {
        QuestionCategory get_id();  // MongoDB aggregation의 _id 필드

        Long getCount();
    }
}