package com.sk.growthnav.api.member.controller;

import com.sk.growthnav.api.home.service.HomeScreenFacadeService;
import com.sk.growthnav.api.member.dto.*;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저")
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
    @Operation(
            summary = "회원가입",
            description = """
                    새로운 사용자 계정을 생성합니다.
                    
                    **주요 기능:**
                    - 이메일 중복 검사
                    - 사용자 정보 등록
                    - 회원 ID 자동 생성
                    
                    **참고사항:**
                    - 이메일은 고유해야 함
                    - 비밀번호는 4자 이상 권장
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberSignupRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "name": "홍길동",
                                              "email": "hong@example.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
    )
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
    @Operation(
            summary = "로그인",
            description = """
                    이메일과 비밀번호로 로그인합니다.
                    
                    **주요 기능:**
                    - 이메일/비밀번호 검증
                    - 회원 정보 반환
                    - 세션 관리 (추후 JWT 토큰 예정)
                    
                    **참고사항:**
                    - 로그인 성공 시 memberId를 반환
                    - 이후 API 호출 시 memberId 사용
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 정보",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberLoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "email": "hong@example.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
    )
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
    @Operation(
            summary = "홈 화면 데이터 조회",
            description = """
                    로그인 후 홈 화면에 표시할 사용자 정보를 조회합니다."""
    )
    @GetMapping("/{memberId}/home")
    public ApiResponse<HomeScreenResponse> getHomeScreen(@PathVariable Long memberId) {
        log.info("홈 화면 데이터 요청: memberId={}", memberId);

        // Facade 서비스 사용으로 변경
        HomeScreenResponse homeScreen = homeScreenFacadeService.getHomeScreenData(memberId);

        log.info("홈 화면 데이터 응답 완료: memberId={}", memberId);
        return ApiResponse.onSuccess(homeScreen);
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