package com.sk.growthnav.api.project.dto;


import com.sk.growthnav.api.project.entity.ProjectScale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectCreateRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    Long memberId;

    @NotBlank(message = "프로젝트 이름은 필수입니다.")
    String projectName;

    @NotBlank(message = "역할은 필수입니다.")
    String userRole;

    @NotBlank(message = "도메인은 필수입니다.")
    String domain;

    @NotNull(message = "프로젝트 규모는 필수입니다.")
    ProjectScale projectScale;

    @NotNull(message = "시작일은 필수입니다.")
    LocalDate startDate;

    LocalDate endDate; // 진행중인 프로젝트는 null 가능

    @NotEmpty(message = "최소 하나의 스킬은 필요합니다.")
    List<String> skills;
}
