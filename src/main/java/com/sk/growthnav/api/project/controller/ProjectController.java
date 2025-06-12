package com.sk.growthnav.api.project.controller;

import com.sk.growthnav.api.project.dto.ProjectCreateRequest;
import com.sk.growthnav.api.project.dto.ProjectCreateResponse;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.service.ProjectService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로젝트")
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
    @Operation(
            summary = "프로젝트 생성",
            description = """
                    새로운 프로젝트를 생성하고 관련 스킬들을 등록합니다.
                    
                    **주요 기능:**
                    - 프로젝트 기본 정보 등록
                    - 여러 스킬 동시 등록
                    - 프로젝트-스킬 관계 자동 연결
                    
                    **프로젝트 규모:**
                    - SMALL: 소규모
                    - MEDIUM: 중규모  
                    - MEDIUM_SMALL: 중소규모
                    - LARGE: 대규모
                    - VERY_LARGE: 초대규모
                    - UNKNOWN: 미기입
                    
                    **참고사항:**
                    - 종료일이 없으면 진행중인 프로젝트로 처리
                    - 스킬은 자동으로 중복 제거
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프로젝트 생성 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "완료된 프로젝트",
                                            value = """
                                                    {
                                                      "memberId": 1,
                                                      "projectName": "Growth Navigator 개발",
                                                      "userRole": "백엔드 개발자",
                                                      "domain": "AI/ML",
                                                      "projectScale": "MEDIUM",
                                                      "startDate": "2025-01-01",
                                                      "endDate": "2025-06-30",
                                                      "skills": ["Java", "Spring Boot", "MongoDB", "PostgreSQL"]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "진행중인 프로젝트",
                                            value = """
                                                    {
                                                      "memberId": 1,
                                                      "projectName": "신규 서비스 개발",
                                                      "userRole": "풀스택 개발자",
                                                      "domain": "웹서비스",
                                                      "projectScale": "LARGE",
                                                      "startDate": "2025-03-01",
                                                      "endDate": null,
                                                      "skills": ["React", "Node.js", "TypeScript", "AWS"]
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
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
    @Operation(
            summary = "회원의 프로젝트 목록 조회",
            description = """
                    특정 회원의 모든 프로젝트를 조회합니다.
                    
                    **제공 정보:**
                    - 프로젝트 기본 정보
                    - 각 프로젝트별 스킬 목록
                    - 프로젝트 기간 정보
                    
                    **정렬 순서:**
                    - 생성일 기준 최신순
                    """
    )
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
    @Operation(
            summary = "프로젝트 삭제",
            description = """
                    프로젝트를 삭제합니다.
                    
                    **주의사항:**
                    - 프로젝트 삭제 시 연관된 모든 스킬도 함께 삭제
                    - 삭제된 데이터는 복구 불가능
                    - 대화 기록에서는 삭제된 프로젝트 정보가 유지될 수 있음
                    """
    )
    @DeleteMapping("/{projectId}")
    public ApiResponse<String> deleteProject(@PathVariable Long projectId) {
        log.info("프로젝트 삭제 요청: projectId={}", projectId);

        projectService.deleteProject(projectId);

        log.info("프로젝트 삭제 완료: projectId={}", projectId);
        return ApiResponse.onSuccess("프로젝트가 삭제되었습니다.");
    }
}