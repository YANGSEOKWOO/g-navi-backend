package com.sk.growthnav.api.admin.dto;

import com.sk.growthnav.api.member.entity.MemberLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class LevelSkillsResponse {

    private MemberLevel level;          // 등급 (CL1, CL2, ...)
    private Integer totalSkillCount;    // 총 기술스택 개수
    private Integer memberCount;        // 해당 등급 회원 수
    private List<SkillStatistic> skills; // 기술스택별 통계

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillStatistic {
        private String skillName;       // 기술스택 이름
        private Integer userCount;      // 해당 기술을 사용하는 회원 수
        private Integer projectCount;   // 해당 기술이 사용된 프로젝트 수
        private Double percentage;      // 해당 등급 내에서의 사용률 (%)

        public static SkillStatistic of(String skillName, Integer userCount, Integer projectCount, Integer totalMembers) {
            double percentage = totalMembers > 0 ? (double) userCount / totalMembers * 100 : 0.0;

            return SkillStatistic.builder()
                    .skillName(skillName)
                    .userCount(userCount)
                    .projectCount(projectCount)
                    .percentage(Math.round(percentage * 10.0) / 10.0) // 소수점 첫째자리까지
                    .build();
        }

        // 전체 스킬 대비 비율
        public static SkillStatistic ofWithSkillRatio(String skillName, Integer userCount, Integer projectCount,
                                                      Integer totalMembers, Integer totalSkillTypes) {
            // 전체 스킬 종류 대비 각 스킬의 비율
            double skillRatio = totalSkillTypes > 0 ? 100.0 / totalSkillTypes : 0.0;

            log.debug("스킬 비율 계산: skillName={}, totalSkillTypes={}, ratio={}%",
                    skillName, totalSkillTypes, skillRatio);

            return SkillStatistic.builder()
                    .skillName(skillName)
                    .userCount(userCount)
                    .projectCount(projectCount)
                    .percentage(Math.round(skillRatio * 10.0) / 10.0) // 소수점 첫째자리까지
                    .build();
        }
    }

    public static LevelSkillsResponse of(MemberLevel level, Integer memberCount, List<SkillStatistic> skills) {
        return LevelSkillsResponse.builder()
                .level(level)
                .totalSkillCount(skills.size())
                .memberCount(memberCount)
                .skills(skills)
                .build();
    }
}