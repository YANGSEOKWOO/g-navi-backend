package com.sk.growthnav.api.admin.service;

import com.sk.growthnav.api.admin.dto.AdminDashboardResponse;
import com.sk.growthnav.api.admin.dto.AdminDashboardResponse.CategoryStatistics;
import com.sk.growthnav.api.admin.dto.AdminDashboardResponse.UserStatistics;
import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.entity.QuestionCategory;
import com.sk.growthnav.api.conversation.repository.ConversationRepository;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final ConversationRepository conversationRepository;

    /**
     * 관리자 대시보드 데이터 조회
     */
    public AdminDashboardResponse getDashboardData() {
        log.info("관리자 대시보드 데이터 조회 시작");

        // 1. 사용자 통계 (전체 & 등급별)
        UserStatistics userStatistics = getUserStatistics();

        // 2. 오늘 대화한 인원
        Long todayChatUsers = getTodayChatUsers();

        // 3. 전체 카테고리별 질문 수
        CategoryStatistics categoryStatistics = getCategoryStatistics();

        // 4. 등급별 카테고리 질문 수
        Map<MemberLevel, CategoryStatistics> levelCategoryStatistics = getLevelCategoryStatistics();

        log.info("관리자 대시보드 데이터 조회 완료");

        return AdminDashboardResponse.of(
                userStatistics,
                todayChatUsers,
                categoryStatistics,
                levelCategoryStatistics
        );
    }

    /**
     * 1. 사용자 통계 조회
     */
    private UserStatistics getUserStatistics() {
        log.debug("사용자 통계 조회 시작");

        // 전체 사용자 수
        Long totalUsers = memberRepository.count();

        // 등급별 사용자 수
        Map<MemberLevel, Long> usersByLevel = new HashMap<>();
        for (MemberLevel level : MemberLevel.values()) {
            Long count = memberRepository.countByLevel(level);
            usersByLevel.put(level, count);
        }

        log.debug("사용자 통계: 전체 {}명, 등급별 분포 {}", totalUsers, usersByLevel);
        return UserStatistics.of(totalUsers, usersByLevel);
    }

    /**
     * 2. 오늘 대화한 인원 수 조회
     */
    private Long getTodayChatUsers() {
        log.debug("오늘 대화한 인원 조회 시작");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        try {
            // MongoDB Aggregation을 사용해서 고유 사용자 수 조회
            Long uniqueUsers = conversationRepository.countUniqueUsersUpdatedBetween(startOfDay, endOfDay);
            log.debug("오늘 대화한 고유 사용자 수: {}", uniqueUsers);
            return uniqueUsers != null ? uniqueUsers : 0L;
        } catch (Exception e) {
            log.warn("MongoDB aggregation 실패, 대안 방법 사용: {}", e.getMessage());

            // 대안: 일반 쿼리 + Java Stream 처리
            List<ConversationDocument> todayConversations =
                    conversationRepository.findConversationsUpdatedBetween(startOfDay, endOfDay);

            long uniqueUsers = todayConversations.stream()
                    .map(ConversationDocument::getMemberId)
                    .distinct()
                    .count();

            log.debug("오늘 대화한 고유 사용자 수 (대안 방법): {}", uniqueUsers);
            return uniqueUsers;
        }
    }

    /**
     * 3. 전체 카테고리별 질문 수 조회
     */
    private CategoryStatistics getCategoryStatistics() {
        log.debug("카테고리별 질문 통계 조회 시작");

        try {
            // MongoDB Aggregation 사용
            List<ConversationRepository.CategoryCountResult> results =
                    conversationRepository.countAllUserQuestionsByCategory();

            Map<QuestionCategory, Long> categoryMap = results.stream()
                    .collect(Collectors.toMap(
                            ConversationRepository.CategoryCountResult::get_id,
                            ConversationRepository.CategoryCountResult::getCount
                    ));

            Long careerQuestions = categoryMap.getOrDefault(QuestionCategory.CAREER, 0L);
            Long skillQuestions = categoryMap.getOrDefault(QuestionCategory.SKILL, 0L);
            Long projectQuestions = categoryMap.getOrDefault(QuestionCategory.PROJECT, 0L);
            Long otherQuestions = categoryMap.getOrDefault(QuestionCategory.OTHER, 0L);

            log.debug("카테고리별 질문 수: 커리어={}, 스킬={}, 프로젝트={}, 기타={}",
                    careerQuestions, skillQuestions, projectQuestions, otherQuestions);

            return CategoryStatistics.of(careerQuestions, skillQuestions, projectQuestions, otherQuestions);

        } catch (Exception e) {
            log.warn("MongoDB aggregation 실패, 대안 방법 사용: {}", e.getMessage());

            // 대안: 모든 대화를 불러와서 Java에서 처리
            return getCategoryStatisticsAlternative();
        }
    }

    /**
     * 카테고리 통계 대안 방법 (Java 처리)
     */
    private CategoryStatistics getCategoryStatisticsAlternative() {
        List<ConversationDocument> allConversations = conversationRepository.findAll();

        long careerQuestions = 0;
        long skillQuestions = 0;
        long projectQuestions = 0;
        long otherQuestions = 0;

        for (ConversationDocument conversation : allConversations) {
            for (ConversationDocument.MessageDocument message : conversation.getMessages()) {
                if (message.getSenderType() == com.sk.growthnav.global.document.SenderType.USER) {
                    QuestionCategory category = message.getCategory();
                    if (category != null) {
                        switch (category) {
                            case CAREER -> careerQuestions++;
                            case SKILL -> skillQuestions++;
                            case PROJECT -> projectQuestions++;
                            case OTHER -> otherQuestions++;
                        }
                    }
                }
            }
        }

        return CategoryStatistics.of(careerQuestions, skillQuestions, projectQuestions, otherQuestions);
    }

    /**
     * 4. 등급별 카테고리 질문 수 조회
     */
    private Map<MemberLevel, CategoryStatistics> getLevelCategoryStatistics() {
        log.debug("등급별 카테고리 통계 조회 시작");

        Map<MemberLevel, CategoryStatistics> result = new HashMap<>();

        for (MemberLevel level : MemberLevel.values()) {
            // 해당 등급의 모든 사용자 ID 조회
            List<Member> membersOfLevel = memberRepository.findByLevel(level);
            List<Long> memberIds = membersOfLevel.stream()
                    .map(Member::getId)
                    .collect(Collectors.toList());

            if (memberIds.isEmpty()) {
                result.put(level, CategoryStatistics.of(0L, 0L, 0L, 0L));
                continue;
            }

            // 해당 등급 사용자들의 대화 조회
            List<ConversationDocument> conversations = conversationRepository.findByMemberIdIn(memberIds);

            // 카테고리별 질문 수 계산
            long careerQuestions = 0;
            long skillQuestions = 0;
            long projectQuestions = 0;
            long otherQuestions = 0;

            for (ConversationDocument conversation : conversations) {
                for (ConversationDocument.MessageDocument message : conversation.getMessages()) {
                    if (message.getSenderType() == com.sk.growthnav.global.document.SenderType.USER) {
                        QuestionCategory category = message.getCategory();
                        if (category != null) {
                            switch (category) {
                                case CAREER -> careerQuestions++;
                                case SKILL -> skillQuestions++;
                                case PROJECT -> projectQuestions++;
                                case OTHER -> otherQuestions++;
                            }
                        }
                    }
                }
            }

            CategoryStatistics stats = CategoryStatistics.of(
                    careerQuestions, skillQuestions, projectQuestions, otherQuestions);
            result.put(level, stats);

            log.debug("등급 {} 통계: {}", level, stats);
        }

        return result;
    }
}