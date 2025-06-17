package com.sk.growthnav.api.news.service;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.api.news.dto.NewsCreateRequest;
import com.sk.growthnav.api.news.dto.NewsManageRequest;
import com.sk.growthnav.api.news.dto.NewsResponse;
import com.sk.growthnav.api.news.entity.News;
import com.sk.growthnav.api.news.entity.NewsStatus;
import com.sk.growthnav.api.news.repository.NewsRepository;
import com.sk.growthnav.global.apiPayload.code.FailureCode;
import com.sk.growthnav.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final MemberService memberService;
    private final TitleExtractorService titleExtractorService;

    @Transactional
    public NewsResponse createNews(NewsCreateRequest request) {
        Member writer = memberService.findById(request.getWriterId());

        // 제목 자동 추출
        String finalTitle = determineFinalTitle(request);

        // News 엔티티 생성 (Builder 패턴)
        News news = News.builder()
                .title(finalTitle)
                .url(request.getUrl())
                .writer(writer)
                .status(NewsStatus.PENDING)  // 기본값: 승인 대기
                .build();

        News savedNews = newsRepository.save(news);
        log.info("뉴스 생성 완료: newsId={}, title={}, writer={}, status=PENDING",
                savedNews.getId(), finalTitle, writer.getName());

        return NewsResponse.from(savedNews);
    }

    /**
     * 제목 결정: URL에서 자동 추출
     */
    private String determineFinalTitle(NewsCreateRequest request) {
        log.info("URL에서 제목 자동 추출: url={}", request.getUrl());
        String extractedTitle = titleExtractorService.extractTitle(request.getUrl());
        log.info("추출된 제목: {}", extractedTitle);
        return extractedTitle;
    }

    // ========== 조회 메서드들 (기존) ==========

    // 일반 사용자용 - 승인된 뉴스만 조회
    public List<NewsResponse> getApprovedNews() {
        List<News> news = newsRepository.findByStatusOrderByCreatedAtDesc(NewsStatus.APPROVED);
        return news.stream()
                .map(NewsResponse::forPublic)  // 관리 버튼 없는 버전
                .toList();
    }

    // Writer용 - 내가 작성한 모든 뉴스 조회
    public List<NewsResponse> getNewsByWriter(Long writerId) {
        List<News> news = newsRepository.findByWriterIdOrderByCreatedAtDesc(writerId);
        return news.stream()
                .map(NewsResponse::from)
                .toList();
    }

    // Admin용 - 모든 뉴스 조회 (관리 버튼 포함)
    public List<NewsResponse> getAllNewsForAdmin() {
        List<News> news = newsRepository.findAllByOrderByCreatedAtDesc();
        return news.stream()
                .map(NewsResponse::from)  // 관리 버튼 포함 버전
                .toList();
    }

    // Admin용 - 승인 대기중인 뉴스만 조회
    public List<NewsResponse> getPendingNews() {
        List<News> news = newsRepository.findByStatusOrderByCreatedAtDesc(NewsStatus.PENDING);
        return news.stream()
                .map(NewsResponse::from)
                .toList();
    }

    // 승인 대기중인 뉴스 개수 조회 (대시보드용)
    public long getPendingNewsCount() {
        return newsRepository.countByStatus(NewsStatus.PENDING);
    }

    // ========== 새로 추가할 메서드들 ==========

    /**
     * 뉴스 상세 조회
     */
    public NewsResponse getNewsDetail(Long newsId) {
        News news = findNewsById(newsId);
        return NewsResponse.from(news);
    }

    /**
     * Admin 뉴스 관리 액션 처리 (승인/거부/승인해제)
     */
    @Transactional
    public String manageNews(NewsManageRequest request) {
        News news = findNewsById(request.getNewsId());

        String actionResult;

        switch (request.getAction()) {
            case APPROVE:
                if (!news.isPending()) {
                    throw new IllegalStateException("승인 대기중인 뉴스만 승인할 수 있습니다.");
                }
                news.approve();
                actionResult = "뉴스가 승인되었습니다.";
                log.info("뉴스 승인: newsId={}, title={}", news.getId(), news.getTitle());
                break;

            case UNAPPROVE:
                if (!news.isApproved()) {
                    throw new IllegalStateException("승인된 뉴스만 승인 해제할 수 있습니다.");
                }
                news.unapprove();
                actionResult = "뉴스 승인이 해제되었습니다.";
                log.info("뉴스 승인 해제: newsId={}, title={}", news.getId(), news.getTitle());
                break;

            case REJECT:
                if (news.isRejected()) {
                    throw new IllegalStateException("이미 거부된 뉴스입니다.");
                }
                news.reject();
                actionResult = "뉴스가 거부되었습니다.";
                log.info("뉴스 거부: newsId={}, title={}", news.getId(), news.getTitle());
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 관리 액션입니다: " + request.getAction());
        }

        newsRepository.save(news);
        return actionResult;
    }

    /**
     * 뉴스 수정 (Writer/Admin)
     */
    @Transactional
    public NewsResponse updateNews(Long newsId, String title, String url) {
        News news = findNewsById(newsId);

        // 제목이 제공되지 않으면 URL에서 다시 추출
        String finalTitle = (title != null && !title.trim().isEmpty()) ?
                title.trim() : titleExtractorService.extractTitle(url);

        news.updateNews(finalTitle, url);
        News savedNews = newsRepository.save(news);

        log.info("뉴스 수정 완료: newsId={}, newTitle={}, newUrl={}",
                newsId, finalTitle, url);

        return NewsResponse.from(savedNews);
    }

    /**
     * 뉴스 완전 삭제 (Admin 전용)
     */
    @Transactional
    public void deleteNews(Long newsId) {
        News news = findNewsById(newsId);

        log.info("뉴스 삭제: newsId={}, title={}, writer={}",
                news.getId(), news.getTitle(), news.getWriter().getName());

        newsRepository.delete(news);
    }

    /**
     * 제목으로 뉴스 검색 (승인된 것만)
     */
    public List<NewsResponse> searchNewsByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getApprovedNews(); // 키워드가 없으면 전체 승인된 뉴스 반환
        }

        List<News> news = newsRepository.findByTitleContainingAndStatusOrderByCreatedAtDesc(
                keyword.trim(), NewsStatus.APPROVED);

        return news.stream()
                .map(NewsResponse::forPublic)
                .toList();
    }

    /**
     * 뉴스 통계 정보 조회 (Admin 대시보드용)
     */
    public NewsStatistics getNewsStatistics() {
        long totalNews = newsRepository.count();
        long approvedNews = newsRepository.countByStatus(NewsStatus.APPROVED);
        long pendingNews = newsRepository.countByStatus(NewsStatus.PENDING);
        long rejectedNews = newsRepository.countByStatus(NewsStatus.REJECTED);

        return NewsStatistics.builder()
                .totalNews(totalNews)
                .approvedNews(approvedNews)
                .pendingNews(pendingNews)
                .rejectedNews(rejectedNews)
                .build();
    }

    /**
     * 특정 상태의 뉴스 조회
     */
    public List<NewsResponse> getNewsByStatus(NewsStatus status) {
        List<News> news = newsRepository.findByStatusOrderByCreatedAtDesc(status);
        return news.stream()
                .map(NewsResponse::from)
                .toList();
    }

    /**
     * 뉴스 작성자 확인 (수정/삭제 권한 체크용)
     */
    public boolean isNewsWriter(Long newsId, Long memberId) {
        News news = findNewsById(newsId);
        return news.getWriter().getId().equals(memberId);
    }

    /**
     * 최근 N개 승인된 뉴스 조회
     */
    public List<NewsResponse> getRecentApprovedNews(int limit) {
        List<News> news = newsRepository.findByStatusOrderByCreatedAtDesc(NewsStatus.APPROVED);
        return news.stream()
                .limit(limit)
                .map(NewsResponse::forPublic)
                .toList();
    }

    // ========== Helper 메서드들 ==========

    /**
     * 뉴스 ID로 뉴스 조회 (존재하지 않으면 예외 발생)
     */
    private News findNewsById(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(FailureCode._NOT_FOUND));
    }

    // ========== 내부 DTO 클래스 ==========

    /**
     * 뉴스 통계 정보
     */
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class NewsStatistics {
        private long totalNews;      // 전체 뉴스 수
        private long approvedNews;   // 승인된 뉴스 수
        private long pendingNews;    // 승인 대기 뉴스 수
        private long rejectedNews;   // 거부된 뉴스 수

        // 승인율 계산
        public double getApprovalRate() {
            return totalNews > 0 ? (double) approvedNews / totalNews * 100 : 0.0;
        }

        // 대기율 계산
        public double getPendingRate() {
            return totalNews > 0 ? (double) pendingNews / totalNews * 100 : 0.0;
        }
    }
}