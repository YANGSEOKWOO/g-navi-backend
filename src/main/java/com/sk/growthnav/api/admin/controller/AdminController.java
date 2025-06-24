package com.sk.growthnav.api.admin.controller;

import com.sk.growthnav.api.admin.dto.AdminDashboardResponse;
import com.sk.growthnav.api.admin.dto.LevelSkillsResponse;
import com.sk.growthnav.api.admin.dto.MemberListResponse;
import com.sk.growthnav.api.admin.dto.RoleChangeRequest;
import com.sk.growthnav.api.admin.service.AdminDashboardService;
import com.sk.growthnav.api.admin.service.AdminService;
import com.sk.growthnav.api.member.dto.LevelChangeRequest;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import com.sk.growthnav.global.auth.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "관리자")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final AdminDashboardService adminDashboardService;
    private final MemberService memberService;
    private final AuthHelper authHelper;

    // ===== 대시보드 관련 =====

    @Operation(
            summary = "관리자 대시보드 데이터 조회",
            description = """
                    관리자 대시보드에 필요한 모든 통계 데이터를 조회합니다.
                    
                    **제공되는 데이터:**
                    1. 전체 사용자 수 & CL1~CL5 등급별 인원
                    2. 오늘 대화한 인원 수
                    3. 커리어/스킬/프로젝트/기타 카테고리별 질문 수
                    4. 등급별 카테고리 질문 수 분석
                    """
    )
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(@RequestParam Long adminId) {
        log.info("관리자 대시보드 조회 요청: adminId={}", adminId);

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        AdminDashboardResponse dashboardData = adminDashboardService.getDashboardData();

        log.info("관리자 대시보드 조회 완료: adminId={}, totalUsers={}, todayChatUsers={}",
                adminId,
                dashboardData.getUserStatistics().getTotalUsers(),
                dashboardData.getTodayChatUsers());

        return ApiResponse.onSuccess(dashboardData);
    }

    @Operation(
            summary = "대시보드 데이터 새로고침",
            description = "대시보드 데이터를 새로고침합니다."
    )
    @PostMapping("/dashboard/refresh")
    public ApiResponse<AdminDashboardResponse> refreshDashboard(@RequestParam Long adminId) {
        log.info("관리자 대시보드 새로고침 요청: adminId={}", adminId);

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        AdminDashboardResponse dashboardData = adminDashboardService.getDashboardData();

        log.info("관리자 대시보드 새로고침 완료: adminId={}", adminId);
        return ApiResponse.onSuccess(dashboardData);
    }

    // ===== 등급별 기술스택 관련 =====

    @Operation(
            summary = "모든 등급별 기술스택 목록 조회 (Admin 전용)",
            description = """
                    CL1~CL5 모든 등급별로 해당 등급 사용자들이 보유한 기술스택을 조회합니다.
                    
                    **제공되는 데이터:**
                    - 각 등급별 고유 기술스택 목록 (중복 제거)
                    - 기술스택별 사용자 수 (해당 등급 내에서)
                    - 등급별 총 기술스택 개수
                    
                    **응답 예시:**
                    ```json
                    {
                      "CL1": {
                        "level": "CL1",
                        "totalSkillCount": 8,
                        "memberCount": 12,
                        "skills": [
                          {"skillName": "Java", "userCount": 5},
                          {"skillName": "Python", "userCount": 3}
                        ]
                      },
                      "CL2": {
                        "level": "CL2", 
                        "totalSkillCount": 15,
                        "memberCount": 8,
                        "skills": [...]
                      }
                    }
                    ```
                    """
    )
    @GetMapping("/levels/skills")
    public ApiResponse<Map<MemberLevel, LevelSkillsResponse>> getAllLevelSkills(@RequestParam Long adminId) {
        log.info("등급별 기술스택 조회 요청: adminId={}", adminId);

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        Map<MemberLevel, LevelSkillsResponse> levelSkills = adminDashboardService.getAllLevelSkills();

        log.info("등급별 기술스택 조회 완료: adminId={}, levelsCount={}", adminId, levelSkills.size());
        return ApiResponse.onSuccess(levelSkills);
    }

    @Operation(
            summary = "특정 등급의 기술스택 목록 조회 (Admin 전용)",
            description = """
                    특정 등급(CL1~CL5)의 사용자들이 보유한 기술스택을 상세 조회합니다.
                    
                    **제공되는 데이터:**
                    - 해당 등급의 모든 고유 기술스택 (중복 제거)
                    - 각 기술스택을 보유한 사용자 수
                    - 기술스택별 프로젝트 수
                    - 인기도 순으로 정렬
                    
                    **URL 예시:**
                    - GET /api/admin/levels/CL3/skills?adminId=1
                    """,
            parameters = {
                    @Parameter(
                            name = "level",
                            description = "조회할 등급 (CL1, CL2, CL3, CL4, CL5)",
                            required = true,
                            example = "CL3"
                    )
            }
    )
    @GetMapping("/levels/{level}/skills")
    public ApiResponse<LevelSkillsResponse> getLevelSkills(
            @PathVariable MemberLevel level,
            @RequestParam Long adminId) {

        log.info("특정 등급 기술스택 조회 요청: adminId={}, level={}", adminId, level);

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        LevelSkillsResponse levelSkills = adminDashboardService.getLevelSkills(level);

        log.info("특정 등급 기술스택 조회 완료: adminId={}, level={}, skillCount={}",
                adminId, level, levelSkills.getSkills().size());
        return ApiResponse.onSuccess(levelSkills);
    }

    // ===== 회원 관리 관련 =====

    @Operation(summary = "모든 회원 조회 (관리자 전용)")
    @GetMapping("/members")
    public ApiResponse<List<MemberListResponse>> getAllMembers(@RequestParam Long adminId) {
        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        List<MemberListResponse> members = adminService.getAllMembers();
        return ApiResponse.onSuccess(members);
    }

    @Operation(summary = "회원 역할 변경 (관리자 전용)")
    @PutMapping("/members/role")
    public ApiResponse<String> changeUserRole(
            @RequestParam Long adminId,
            @Valid @RequestBody RoleChangeRequest request) {

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        adminService.changeMemberRole(request.getMemberId(), request.getNewRole());
        return ApiResponse.onSuccess("회원 역할이 변경되었습니다.");
    }

    @Operation(
            summary = "회원 등급 변경",
            description = """
                    회원의 등급을 변경합니다.
                    
                    **등급 종류:**
                    - CL1, CL2, CL3, CL4, CL5
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등급 변경 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LevelChangeRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "memberId": 1,
                                              "newLevel": "CL3"
                                            }
                                            """
                            )
                    )
            )
    )
    @PutMapping("/members/level")
    public ApiResponse<String> changeMemberLevel(@Valid @RequestBody LevelChangeRequest request) {
        memberService.changeMemberLevel(request.getMemberId(), request.getNewLevel());
        return ApiResponse.onSuccess("회원 등급이 변경되었습니다.");
    }
}