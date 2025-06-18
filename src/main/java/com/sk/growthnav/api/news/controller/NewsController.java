package com.sk.growthnav.api.news.controller;

import com.sk.growthnav.api.news.dto.NewsCreateRequest;
import com.sk.growthnav.api.news.dto.NewsManageRequest;
import com.sk.growthnav.api.news.dto.NewsResponse;
import com.sk.growthnav.api.news.service.NewsService;
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

@Tag(name = "뉴스 기사")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {

    private final NewsService newsService;
    private final AuthHelper authHelper;

    @Operation(
            summary = "뉴스 기사 생성 (EXPERT/Admin 전용)",
            description = "Expert나 Admin이 새로운 뉴스 기사를 작성합니다. 작성 후 Admin의 승인을 기다립니다."
    )
    @PostMapping
    public ApiResponse<NewsResponse> createNews(@Valid @RequestBody NewsCreateRequest request) {

        // EXPERT 권한 확인
        authHelper.validateExpertRole(request.getExpertId());

        NewsResponse response = newsService.createNews(request);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "승인된 뉴스 목록 조회 (모든 사용자)",
            description = "일반 사용자들이 볼 수 있는 승인된 뉴스 기사 목록을 조회합니다."
    )
    @GetMapping
    public ApiResponse<List<NewsResponse>> getApprovedNews() {
        List<NewsResponse> news = newsService.getApprovedNews();
        return ApiResponse.onSuccess(news);
    }

    @Operation(
            summary = "내가 작성한 뉴스 조회 (EXPERT/Admin)",
            description = "작성자가 자신이 작성한 모든 뉴스 기사를 조회합니다. (승인 상태 무관)"
    )
    @GetMapping("/my")
    public ApiResponse<List<NewsResponse>> getMyNews(@RequestParam Long expertId) {

        // expert 권한 확인
        authHelper.validateExpertRole(expertId);

        List<NewsResponse> news = newsService.getNewsByExpert(expertId);
        return ApiResponse.onSuccess(news);
    }

    @Operation(
            summary = "관리자용 모든 뉴스 조회 (Admin 전용)",
            description = """
                    관리자가 모든 뉴스 기사를 조회합니다. 
                    각 기사에는 "승인", "승인 해제", "거부" 관리 액션이 포함됩니다.
                    
                    **응답 예시:**
                    ```json
                    {
                      "newsId": 1,
                      "title": "메타의 AI 앱 프라이버시 악몽",
                      "expert": "김효준",
                      "status": "승인 대기",
                      "date": "2025-06-16",
                      "canApprove": true,
                      "canUnapprove": false,
                      "canReject": true
                    }
                    ```
                    """
    )
    @GetMapping("/admin/all")
    public ApiResponse<List<NewsResponse>> getAllNewsForAdmin(@RequestParam Long adminId) {

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        List<NewsResponse> news = newsService.getAllNewsForAdmin();
        return ApiResponse.onSuccess(news);
    }

    @Operation(
            summary = "승인 대기중인 뉴스 조회 (Admin 전용)",
            description = "관리자가 승인 대기중인 뉴스 기사만 조회합니다."
    )
    @GetMapping("/admin/pending")
    public ApiResponse<List<NewsResponse>> getPendingNews(@RequestParam Long adminId) {

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        List<NewsResponse> news = newsService.getPendingNews();
        return ApiResponse.onSuccess(news);
    }

    @Operation(
            summary = "뉴스 관리 액션 (Admin 전용)",
            description = """
                    관리자가 뉴스 기사를 관리합니다.
                    
                    **관리 액션:**
                    - APPROVE: 승인 (승인 대기 → 승인됨)
                    - UNAPPROVE: 승인 해제 (승인됨 → 승인 대기) 
                    - REJECT: 거부 (승인 대기/승인됨 → 거부됨)
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "뉴스 관리 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NewsManageRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "승인",
                                            value = """
                                                    {
                                                      "newsId": 1,
                                                      "adminId": 1,
                                                      "action": "APPROVE"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "승인 해제",
                                            value = """
                                                    {
                                                      "newsId": 1,
                                                      "adminId": 1,
                                                      "action": "UNAPPROVE"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "거부",
                                            value = """
                                                    {
                                                      "newsId": 1,
                                                      "adminId": 1,
                                                      "action": "REJECT"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @PutMapping("/admin/manage")
    public ApiResponse<String> manageNews(@Valid @RequestBody NewsManageRequest request) {

        // 관리자 권한 확인
        authHelper.validateAdminRole(request.getAdminId());

        String result = newsService.manageNews(request);
        return ApiResponse.onSuccess(result);
    }

    @Operation(
            summary = "뉴스 삭제 (Admin 전용)",
            description = "관리자가 뉴스 기사를 완전히 삭제합니다."
    )
    @DeleteMapping("/{newsId}")
    public ApiResponse<String> deleteNews(
            @PathVariable Long newsId,
            @RequestParam Long adminId) {

        // 관리자 권한 확인
        authHelper.validateAdminRole(adminId);

        newsService.deleteNews(newsId);
        return ApiResponse.onSuccess("뉴스가 삭제되었습니다.");
    }
}