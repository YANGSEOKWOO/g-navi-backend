package com.sk.growthnav.api.admin.dto;

import com.sk.growthnav.api.member.entity.MemberLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    // 1. 전체 사용자 수 관련
    private UserStatistics userStatistics;

    // 2. 오늘 대화한 인원
    private Long todayChatUsers;

    // 3. 카테고리별 질문 수
    private CategoryStatistics categoryStatistics;

    // 4. 등급별 카테고리 질문 수
    private Map<MemberLevel, CategoryStatistics> levelCategoryStatistics;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStatistics {
        private Long totalUsers;
        private Map<MemberLevel, Long> usersByLevel; // CL1: 10명, CL2: 5명 등

        public static UserStatistics of(Long totalUsers, Map<MemberLevel, Long> usersByLevel) {
            return UserStatistics.builder()
                    .totalUsers(totalUsers)
                    .usersByLevel(usersByLevel)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryStatistics {
        private Long careerQuestions;   // 커리어질문 수
        private Long skillQuestions;    // 스킬질문 수
        private Long projectQuestions;  // 프로젝트질문 수
        private Long otherQuestions;    // 기타질문 수
        private Long totalQuestions;    // 전체질문 수

        public static CategoryStatistics of(
                Long careerQuestions,
                Long skillQuestions,
                Long projectQuestions,
                Long otherQuestions) {

            Long total = careerQuestions + skillQuestions + projectQuestions + otherQuestions;

            return CategoryStatistics.builder()
                    .careerQuestions(careerQuestions)
                    .skillQuestions(skillQuestions)
                    .projectQuestions(projectQuestions)
                    .otherQuestions(otherQuestions)
                    .totalQuestions(total)
                    .build();
        }

        // 카테고리별 비율 계산
        public double getCareerPercentage() {
            return totalQuestions > 0 ? (double) careerQuestions / totalQuestions * 100 : 0.0;
        }

        public double getSkillPercentage() {
            return totalQuestions > 0 ? (double) skillQuestions / totalQuestions * 100 : 0.0;
        }

        public double getProjectPercentage() {
            return totalQuestions > 0 ? (double) projectQuestions / totalQuestions * 100 : 0.0;
        }

        public double getOtherPercentage() {
            return totalQuestions > 0 ? (double) otherQuestions / totalQuestions * 100 : 0.0;
        }
    }

    public static AdminDashboardResponse of(
            UserStatistics userStatistics,
            Long todayChatUsers,
            CategoryStatistics categoryStatistics,
            Map<MemberLevel, CategoryStatistics> levelCategoryStatistics) {

        return AdminDashboardResponse.builder()
                .userStatistics(userStatistics)
                .todayChatUsers(todayChatUsers)
                .categoryStatistics(categoryStatistics)
                .levelCategoryStatistics(levelCategoryStatistics)
                .build();
    }
}