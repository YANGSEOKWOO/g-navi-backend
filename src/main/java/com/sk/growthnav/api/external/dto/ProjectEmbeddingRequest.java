package com.sk.growthnav.api.external.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * FastAPI 프로젝트 임베딩 API 요청 DTO
 * FastAPI의 ProjectData 모델과 매핑
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectEmbeddingRequest {

    String employee_id;           // 직원 고유 ID (snake_case로 FastAPI와 매핑)
    String name;                  // 직원 이름
    String project_name;          // 프로젝트명
    String domain;               // 도메인/업계
    String role;                 // 수행 역할
    String scale;                // 프로젝트 규모
    Integer start_year;          // 시작 연도
    Integer end_year;            // 종료 연도 (null 가능)
    List<String> skills;         // 활용 기술/스킬
    String description;          // 프로젝트 설명 (null 가능)
    String achievements;         // 주요 성과 (null 가능)
    Boolean key_experience;      // 핵심 경력 여부

    /**
     * Spring의 Project 엔티티와 Member 정보로부터 FastAPI 요청 생성
     */
    public static ProjectEmbeddingRequest from(com.sk.growthnav.api.project.entity.Project project,
                                               com.sk.growthnav.api.member.entity.Member member,
                                               List<String> skills) {
        return ProjectEmbeddingRequest.builder()
                .employee_id(String.valueOf(member.getId()))
                .name(member.getName())
                .project_name(project.getName())
                .domain(project.getDomain())
                .role(project.getUserRole())
                .scale(project.getProjectScale().getLabel())
                .start_year(project.getStartDate() != null ? project.getStartDate().getYear() : null)
                .end_year(project.getEndDate() != null ? project.getEndDate().getYear() : null)
                .skills(skills)
                .description(null) // 현재 프로젝트 엔티티에는 description 필드가 없음
                .achievements(null) // 현재 프로젝트 엔티티에는 achievements 필드가 없음
                .key_experience(false) // 기본값으로 false 설정
                .build();
    }


}