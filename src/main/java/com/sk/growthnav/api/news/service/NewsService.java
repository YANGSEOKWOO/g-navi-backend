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
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final MemberService memberService;
    private final TitleExtractorService titleExtractorService;
    private final NewsThumbnailService newsThumbnailService;

    @Transactional
    public NewsResponse createNews(NewsCreateRequest request) {
        log.info("뉴스 생성 시작: expertId={}, url={}", request.getExpertId(), request.getUrl());

        Member expert = memberService.findById(request.getExpertId());

        // 제목 자동 추출
        String finalTitle = determineFinalTitle(request);

        // News 엔티티 생성 (Builder 패턴)
        News news = News.builder()
                .title(finalTitle)
                .url(request.getUrl())
                .expert(expert)
                .status(NewsStatus.PENDING)  // 기본값: 승인 대기
                .build();

        News savedNews = newsRepository.save(news);
        log.info("뉴스 생성 완료: newsId={}, title={}, expert={}, status=PENDING",
                savedNews.getId(), finalTitle, expert.getName());

        // 비동기 썸네일 추출 실행
        extractThumbnailAsync(savedNews);

        return NewsResponse.from(savedNews);
    }

    /**
     * 제목 결정: URL에서 자동 추출
     */
    private String determineFinalTitle(NewsCreateRequest request) {
        // 요청에 제목이 있으면 사용, 없으면 URL에서 추출
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            log.info("사용자 제공 제목 사용: {}", request.getTitle().trim());
            return request.getTitle().trim();
        }

        log.info("URL에서 제목 자동 추출 시작: url={}", request.getUrl());
        try {
            String extractedTitle = titleExtractorService.extractTitle(request.getUrl());
            log.info("제목 추출 성공: url={}, title={}", request.getUrl(), extractedTitle);
            return extractedTitle;
        } catch (Exception e) {
            log.warn("제목 추출 실패, 기본 제목 사용: url={}, error={}", request.getUrl(), e.getMessage());
            return "뉴스 기사"; // 폴백 제목
        }
    }

    /**
     * 비동기 썸네일 추출 (기본 스레드 풀 사용)
     */
    private void extractThumbnailAsync(News news) {
        log.info("비동기 썸네일 추출 요청: newsId={}, url={}", news.getId(), news.getUrl());

        // 기본 ForkJoinPool 사용 (별도 스레드 풀 불필요)
        CompletableFuture
                .supplyAsync(() -> {
                    log.debug("썸네일 추출 작업 시작: newsId={}, thread={}",
                            news.getId(), Thread.currentThread().getName());

                    long startTime = System.currentTimeMillis();
                    NewsThumbnailService.ThumbnailResult result =
                            newsThumbnailService.extractAndSaveThumbnail(news.getUrl(), news.getId());
                    long duration = System.currentTimeMillis() - startTime;

                    log.debug("썸네일 추출 완료: newsId={}, duration={}ms, success={}",
                            news.getId(), duration, result.isSuccess());

                    return result;
                }) // 기본 스레드 풀 사용
                .thenAccept(thumbnailResult -> {
                    // 결과 처리
                    processThumbnailResult(news.getId(), thumbnailResult);
                })
                .exceptionally(throwable -> {
                    // 예외 처리
                    log.error("썸네일 추출 중 예상치 못한 오류: newsId={}, error={}",
                            news.getId(), throwable.getMessage(), throwable);
                    return null;
                });
    }

    /**
     * 썸네일 추출 결과 처리
     */
    private void processThumbnailResult(Long newsId, NewsThumbnailService.ThumbnailResult thumbnailResult) {
        if (thumbnailResult.isSuccess()) {
            log.info("썸네일 추출 성공: newsId={}, filePath={}, accessUrl={}",
                    newsId, thumbnailResult.getFilePath(), thumbnailResult.getAccessUrl());

            // 뉴스 엔티티에 썸네일 정보 업데이트
            updateNewsThumbnail(newsId, thumbnailResult.getFilePath(), thumbnailResult.getAccessUrl());
        } else {
            log.warn("썸네일 추출 실패: newsId={}, error={}",
                    newsId, thumbnailResult.getErrorMessage());
        }
    }

    /**
     * 뉴스 썸네일 정보 업데이트 (별도 트랜잭션)
     */
    @Transactional
    public void updateNewsThumbnail(Long newsId, String thumbnailPath, String thumbnailUrl) {
        try {
            News news = newsRepository.findById(newsId)
                    .orElseThrow(() -> new GeneralException(FailureCode._NOT_FOUND));

            news.setThumbnail(thumbnailPath, thumbnailUrl);
            newsRepository.save(news);

            log.info("뉴스 썸네일 정보 업데이트 완료: newsId={}, thumbnailUrl={}",
                    newsId, thumbnailUrl);
        } catch (Exception e) {
            log.error("뉴스 썸네일 정보 업데이트 실패: newsId={}, error={}",
                    newsId, e.getMessage(), e);
        }
    }

    // ========== 기존 조회 메서드들 (생략) ==========

    public List<NewsResponse> getApprovedNews() {
        List<News> news = newsRepository.findByStatusOrderByCreatedAtDesc(NewsStatus.APPROVED);
        return news.stream()
                .map(NewsResponse::forPublic)
                .toList();
    }

    public List<NewsResponse> getNewsByExpert(Long expertId) {
        List<News> news = newsRepository.findByExpertIdOrderByCreatedAtDesc(expertId);
        return news.stream()
                .map(NewsResponse::from)
                .toList();
    }

    public List<NewsResponse> getAllNewsForAdmin() {
        List<News> news = newsRepository.findAllByOrderByCreatedAtDesc();
        return news.stream()
                .map(NewsResponse::from)
                .toList();
    }

    public List<NewsResponse> getPendingNews() {
        List<News> news = newsRepository.findByStatusOrderByCreatedAtDesc(NewsStatus.PENDING);
        return news.stream()
                .map(NewsResponse::from)
                .toList();
    }

    public long getPendingNewsCount() {
        return newsRepository.countByStatus(NewsStatus.PENDING);
    }

    public NewsResponse getNewsDetail(Long newsId) {
        News news = findNewsById(newsId);
        return NewsResponse.from(news);
    }

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

    @Transactional
    public void deleteNews(Long newsId) {
        News news = findNewsById(newsId);

        log.info("뉴스 삭제: newsId={}, title={}, expert={}",
                news.getId(), news.getTitle(), news.getExpert().getName());

        newsRepository.delete(news);
    }

    private News findNewsById(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new GeneralException(FailureCode._NOT_FOUND));
    }
}