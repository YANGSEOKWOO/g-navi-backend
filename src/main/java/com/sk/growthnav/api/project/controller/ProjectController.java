package com.sk.growthnav.api.project.controller;

import com.sk.growthnav.api.project.dto.ProjectCreateRequest;
import com.sk.growthnav.api.project.dto.ProjectCreateResponse;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.service.ProjectService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     * POST /api/projects
     */
    @PostMapping
    public ApiResponse<ProjectCreateResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        log.info("프로젝트 생성 요청: memberId={}, projectName={}",
                request.getMemberId(), request.getProjectName());

        ProjectCreateResponse response = projectService.createProject(request);

        log.info("프로젝트 생성 완료: projectId={}", response.getProjectId());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 회원의 프로젝트 목록 조회
     * GET /api/projects?memberId=1
     */
    @GetMapping
    public ApiResponse<List<ProjectInfoDTO>> getProjectsByMember(
            @RequestParam @Min(value = 1, message = "회원 ID는 1 이상이어야 합니다") Long memberId) {

        log.info("회원 프로젝트 목록 조회: memberId={}", memberId);

        List<ProjectInfoDTO> projects = projectService.getProjectsByMember(memberId);

        log.info("프로젝트 목록 조회 완료: memberId={}, projectCount={}", memberId, projects.size());
        return ApiResponse.onSuccess(projects);
    }

    /**
     * 프로젝트 상세 조회
     * GET /api/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectInfoDTO> getProjectDetail(@PathVariable Long projectId) {
        log.info("프로젝트 상세 조회: projectId={}", projectId);

        ProjectInfoDTO project = projectService.getProjectDetail(projectId);

        log.info("프로젝트 상세 조회 완료: projectId={}", projectId);
        return ApiResponse.onSuccess(project);
    }

    /**
     * 프로젝트 삭제
     * DELETE /api/projects/{projectId}
     */
    @DeleteMapping("/{projectId}")
    public ApiResponse<String> deleteProject(@PathVariable Long projectId) {
        log.info("프로젝트 삭제 요청: projectId={}", projectId);

        projectService.deleteProject(projectId);

        log.info("프로젝트 삭제 완료: projectId={}", projectId);
        return ApiResponse.onSuccess("프로젝트가 삭제되었습니다.");
    }
}