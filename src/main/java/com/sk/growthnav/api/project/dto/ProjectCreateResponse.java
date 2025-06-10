package com.sk.growthnav.api.project.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectCreateResponse {
    Long projectId;
    String projectName;
    String userRole;
    String domain;
    String projectScale;
    LocalDate startDate;
    LocalDate endDate;
    List<String> skills;
    String message;

    public static ProjectCreateResponse of(
            Long projectId,
            String projectName,
            String userRole,
            String domain,
            String projectScale,
            LocalDate startDate,
            LocalDate endDate,
            List<String> skills) {

        return ProjectCreateResponse.builder()
                .projectId(projectId)
                .projectName(projectName)
                .userRole(userRole)
                .domain(domain)
                .projectScale(projectScale)
                .startDate(startDate)
                .endDate(endDate)
                .skills(skills)
                .message("프로젝트가 성공적으로 등록되었습니다.")
                .build();
    }
}