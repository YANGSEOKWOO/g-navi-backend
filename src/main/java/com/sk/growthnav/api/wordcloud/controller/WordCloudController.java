package com.sk.growthnav.api.wordcloud.controller;

import com.sk.growthnav.api.wordcloud.dto.WordCloudResponse;
import com.sk.growthnav.api.wordcloud.service.WordCloudService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import com.sk.growthnav.global.auth.AuthHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "워드클라우드", description = "사용자 질문 분석 및 워드클라우드 생성 API")
@RestController
@RequestMapping("/api/wordcloud")
@RequiredArgsConstructor
@Slf4j
public class WordCloudController {

    private final WordCloudService wordCloudService;
    private final AuthHelper authHelper;

    @Operation(
            summary = "🔐 전체 사용자 질문 워드클라우드 (Admin 전용)",
            description = "모든 사용자의 질문을 분석하여 워드클라우드 데이터를 제공합니다.",
            tags = {"관리자 전용"}
    )
    @GetMapping("/admin/all")
    public ApiResponse<WordCloudResponse> getAllUserQuestionsWordCloud(
            @Parameter(description = "관리자 ID", required = true, example = "1")
            @RequestParam Long adminId,
            @Parameter(description = "최대 단어 개수", example = "100")
            @RequestParam(defaultValue = "100") Integer maxWords) {

        log.info("전체 사용자 질문 워드클라우드 조회: adminId={}", adminId);
        authHelper.validateAdminRole(adminId);

        WordCloudResponse response = wordCloudService.getAllUserQuestionsWordCloud(maxWords);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "🔐 등급별 사용자 질문 워드클라우드 (Admin 전용)",
            description = "특정 등급 사용자들의 질문을 분석합니다.",
            tags = {"관리자 전용"}
    )
    @GetMapping("/admin/level/{level}")
    public ApiResponse<WordCloudResponse> getLevelUserQuestionsWordCloud(
            @Parameter(description = "분석할 등급", required = true, example = "CL3")
            @PathVariable String level,
            @Parameter(description = "관리자 ID", required = true, example = "1")
            @RequestParam Long adminId,
            @Parameter(description = "최대 단어 개수", example = "100")
            @RequestParam(defaultValue = "100") Integer maxWords) {

        log.info("등급별 워드클라우드 조회: level={}, adminId={}", level, adminId);
        authHelper.validateAdminRole(adminId);

        WordCloudResponse response = wordCloudService.getLevelUserQuestionsWordCloud(level, maxWords);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "🔐 카테고리별 질문 워드클라우드 (Admin 전용)",
            description = "특정 카테고리의 질문들을 분석합니다.",
            tags = {"관리자 전용"}
    )
    @GetMapping("/admin/category/{category}")
    public ApiResponse<WordCloudResponse> getCategoryQuestionsWordCloud(
            @Parameter(description = "분석할 카테고리", required = true, example = "SKILL")
            @PathVariable String category,
            @Parameter(description = "관리자 ID", required = true, example = "1")
            @RequestParam Long adminId,
            @Parameter(description = "최대 단어 개수", example = "100")
            @RequestParam(defaultValue = "100") Integer maxWords) {

        log.info("카테고리별 워드클라우드 조회: category={}, adminId={}", category, adminId);
        authHelper.validateAdminRole(adminId);

        WordCloudResponse response = wordCloudService.getCategoryQuestionsWordCloud(category, maxWords);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "👤 개인 질문 워드클라우드",
            description = "특정 사용자의 질문 패턴을 분석합니다. (본인 또는 관리자만 조회 가능)",
            tags = {"사용자"}
    )
    @GetMapping("/user/{userId}")
    public ApiResponse<WordCloudResponse> getUserQuestionsWordCloud(
            @Parameter(description = "분석할 사용자 ID", required = true, example = "5")
            @PathVariable Long userId,
            @Parameter(description = "요청자 ID", required = true, example = "5")
            @RequestParam Long requesterId,
            @Parameter(description = "최대 단어 개수", example = "50")
            @RequestParam(defaultValue = "50") Integer maxWords) {

        log.info("개인 워드클라우드 조회: userId={}, requesterId={}", userId, requesterId);
        authHelper.validateSelfOrAdmin(requesterId, userId);

        WordCloudResponse response = wordCloudService.getUserQuestionsWordCloud(userId, maxWords);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "📊 공개 워드클라우드 트렌드",
            description = "전체 질문 트렌드를 익명화하여 공개합니다. (상위 50개 키워드만)",
            tags = {"공개"}
    )
    @GetMapping("/public/trends")
    public ApiResponse<WordCloudResponse> getPublicWordCloudTrends() {
        log.info("공개 워드클라우드 트렌드 조회");

        // 공개용이므로 최대 50개만 제한
        WordCloudResponse response = wordCloudService.getAllUserQuestionsWordCloud(50);
        return ApiResponse.onSuccess(response);
    }
}