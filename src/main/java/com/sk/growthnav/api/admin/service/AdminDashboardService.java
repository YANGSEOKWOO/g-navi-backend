package com.sk.growthnav.api.admin.service;

import com.sk.growthnav.api.admin.dto.AdminDashboardResponse;
import com.sk.growthnav.api.admin.dto.AdminDashboardResponse.CategoryStatistics;
import com.sk.growthnav.api.admin.dto.AdminDashboardResponse.UserStatistics;
import com.sk.growthnav.api.admin.dto.LevelSkillsResponse;
import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.entity.QuestionCategory;
import com.sk.growthnav.api.conversation.repository.ConversationRepository;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.project.repository.ProjectRepository;
import com.sk.growthnav.api.skill.entity.Skill;
import com.sk.growthnav.api.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final ConversationRepository conversationRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

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

    /**
     * 모든 등급별 기술스택 조회
     */
    public Map<MemberLevel, LevelSkillsResponse> getAllLevelSkills() {
        log.info("모든 등급별 기술스택 조회 시작");

        Map<MemberLevel, LevelSkillsResponse> result = new LinkedHashMap<>();

        // CL1~CL5 순서대로 조회
        for (MemberLevel level : MemberLevel.values()) {
            LevelSkillsResponse levelSkills = getLevelSkills(level);
            result.put(level, levelSkills);
        }

        log.info("모든 등급별 기술스택 조회 완료: 등급 수={}", result.size());
        return result;
    }

    /**
     * 특정 등급의 기술스택 조회
     */
    public LevelSkillsResponse getLevelSkills(MemberLevel level) {
        log.info("등급별 기술스택 조회 시작: level={}", level);

        // 1. 해당 등급의 모든 회원 조회
        List<Member> membersOfLevel = memberRepository.findByLevel(level);
        log.debug("등급 {} 회원 수: {}", level, membersOfLevel.size());

        if (membersOfLevel.isEmpty()) {
            log.info("등급 {}에 회원이 없음", level);
            return LevelSkillsResponse.of(level, 0, List.of());
        }

        // 2. 해당 회원들의 모든 프로젝트 조회
        List<Long> memberIds = membersOfLevel.stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        List<Project> projects = getProjectsByMemberIds(memberIds);
        log.debug("등급 {} 프로젝트 수: {}", level, projects.size());

        if (projects.isEmpty()) {
            log.info("등급 {}에 프로젝트가 없음", level);
            return LevelSkillsResponse.of(level, membersOfLevel.size(), List.of());
        }

        // 3. 프로젝트들의 모든 스킬 조회 및 통계 계산
        List<LevelSkillsResponse.SkillStatistic> skillStatistics = calculateSkillStatistics(projects, membersOfLevel.size());

        log.info("등급별 기술스택 조회 완료: level={}, memberCount={}, skillCount={}",
                level, membersOfLevel.size(), skillStatistics.size());

        return LevelSkillsResponse.of(level, membersOfLevel.size(), skillStatistics);
    }

    /**
     * 회원 ID 목록으로 프로젝트 조회
     */
    private List<Project> getProjectsByMemberIds(List<Long> memberIds) {
        List<Project> allProjects = new ArrayList<>();

        // 각 회원별로 프로젝트 조회 (배치 처리 최적화 가능)
        for (Long memberId : memberIds) {
            List<Project> memberProjects = projectRepository.findByMemberId(memberId);
            allProjects.addAll(memberProjects);
        }

        return allProjects;
    }

    /**
     * 스킬 통계 계산
     */
    private List<LevelSkillsResponse.SkillStatistic> calculateSkillStatistics(List<Project> projects, int totalMembers) {
        // 1. 모든 프로젝트의 스킬 조회
        Map<String, SkillData> skillDataMap = new HashMap<>();

        for (Project project : projects) {
            List<Skill> projectSkills = skillRepository.findAllByProjectId(project.getId());

            for (Skill skill : projectSkills) {
                String skillName = skill.getName();

                skillDataMap.computeIfAbsent(skillName, k -> new SkillData())
                        .addProject(project.getMember().getId(), project.getId());
            }
        }

        // 2. 통계 계산 및 정렬
        return skillDataMap.entrySet().stream()
                .map(entry -> {
                    String skillName = entry.getKey();
                    SkillData data = entry.getValue();

                    return LevelSkillsResponse.SkillStatistic.of(
                            skillName,
                            data.getUserCount(),
                            data.getProjectCount(),
                            totalMembers
                    );
                })
                .sorted((a, b) -> {
                    // 사용자 수 내림차순 -> 프로젝트 수 내림차순 -> 이름 오름차순
                    int userCompare = Integer.compare(b.getUserCount(), a.getUserCount());
                    if (userCompare != 0) return userCompare;

                    int projectCompare = Integer.compare(b.getProjectCount(), a.getProjectCount());
                    if (projectCompare != 0) return projectCompare;

                    return a.getSkillName().compareToIgnoreCase(b.getSkillName());
                })
                .collect(Collectors.toList());
    }

    /**
     * 스킬 데이터 집계를 위한 내부 클래스
     */
    private static class SkillData {
        private final Set<Long> userIds = new HashSet<>();
        private final Set<Long> projectIds = new HashSet<>();

        public void addProject(Long userId, Long projectId) {
            userIds.add(userId);
            projectIds.add(projectId);
        }

        public int getUserCount() {
            return userIds.size();
        }

        public int getProjectCount() {
            return projectIds.size();
        }
    }

}