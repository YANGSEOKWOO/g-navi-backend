package com.sk.growthnav.api.admin.controller;

import com.sk.growthnav.api.admin.dto.AdminDashboardResponse;
import com.sk.growthnav.api.admin.dto.MemberListResponse;
import com.sk.growthnav.api.admin.dto.RoleChangeRequest;
import com.sk.growthnav.api.admin.service.AdminDashboardService;
import com.sk.growthnav.api.admin.service.AdminService;
import com.sk.growthnav.api.member.dto.LevelChangeRequest;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import com.sk.growthnav.global.auth.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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