package com.sk.growthnav.api.admin.controller;

import com.sk.growthnav.api.admin.dto.MemberListResponse;
import com.sk.growthnav.api.admin.dto.RoleChangeRequest;
import com.sk.growthnav.api.admin.service.AdminService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import com.sk.growthnav.global.auth.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
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
    private final AuthHelper authHelper;

    @Operation(summary = "모든 회원 조회 (관리자 전용)")
    @GetMapping("/members")
    public ApiResponse<List<MemberListResponse>> getAllMembers(
            @RequestParam Long adminId) {

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
}