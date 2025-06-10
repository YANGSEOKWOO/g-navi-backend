package com.sk.growthnav.api.member.controller;

import com.sk.growthnav.api.home.service.HomeScreenFacadeService;
import com.sk.growthnav.api.member.dto.*;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class MemberAutoController {

    private final MemberService memberService;
    private final HomeScreenFacadeService homeScreenFacadeService; // 추가

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ApiResponse<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        log.info("회원가입 요청: email={}, name={}", request.getEmail(), request.getName());

        MemberSignupResponse response = memberService.signup(request);

        log.info("회원가입 성공: memberId={}");
        return ApiResponse.onSuccess(response);
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        log.info("로그인 요청: email={}", request.getEmail());

        MemberLoginResponse response = memberService.login(request);

        log.info("로그인 성공: memberId={}", response.getMemberId());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 이메일 중복 확인 (선택적 기능)
     * GET /api/auth/check-email?email=test@example.com
     */
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("이메일 중복 확인: email={}", email);

        boolean exists = memberService.isEmailExists(email);

        log.info("이메일 중복 확인 결과: email={}, exists={}", email, exists);
        return ApiResponse.onSuccess(exists);
    }

    /**
     * 홈 화면 데이터 조회
     * GET /api/auth/{memberId}/home
     */
    @GetMapping("/{memberId}/home")
    public ApiResponse<HomeScreenResponse> getHomeScreen(@PathVariable Long memberId) {
        log.info("홈 화면 데이터 요청: memberId={}", memberId);

        // Facade 서비스 사용으로 변경
        HomeScreenResponse homeScreen = homeScreenFacadeService.getHomeScreenData(memberId);

        log.info("홈 화면 데이터 응답 완료: memberId={}", memberId);
        return ApiResponse.onSuccess(homeScreen);
    }
}