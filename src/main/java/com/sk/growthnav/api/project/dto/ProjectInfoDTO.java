package com.sk.growthnav.api.project.dto;

import com.sk.growthnav.api.project.entity.Project;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectInfoDTO {
    Long projectId;
    String projectName;
    String role;
    String domain;
    String scale;
    String startDate;
    String endDate;
    List<String> skills;

    // 날짜 포맷터 (ISO 형식: "2023-01-15")
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Entity -> DTO 변환
    public static ProjectInfoDTO from(Project project, List<String> skills) {
        return ProjectInfoDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .role(project.getUserRole())
                .domain(project.getDomain())
                .scale(project.getProjectScale().getLabel())
                .startDate(project.getStartDate() != null ?
                        project.getStartDate().format(DATE_FORMATTER) : null)
                .endDate(project.getEndDate() != null ?
                        project.getEndDate().format(DATE_FORMATTER) : null)
                .skills(skills)
                .build();

    }
}
