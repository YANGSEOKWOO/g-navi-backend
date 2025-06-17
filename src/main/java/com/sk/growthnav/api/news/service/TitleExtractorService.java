package com.sk.growthnav.api.news.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
@Slf4j
public class TitleExtractorService {

    private static final int TIMEOUT_MS = 5000; // 5초 타임아웃
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * URL에서 페이지 제목을 추출합니다.
     *
     * @param url 제목을 추출할 URL
     * @return 추출된 제목, 실패 시 기본 제목
     */
    public String extractTitle(String url) {
        log.info("제목 추출 시작: url={}", url);

        try {
            // URL 유효성 검사
            if (!isValidUrl(url)) {
                log.warn("유효하지 않은 URL: {}", url);
                return generateFallbackTitle(url);
            }

            // 웹페이지 연결 및 파싱
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            // 제목 추출 시도 (우선순위별)
            String title = extractTitleFromDocument(document);

            if (title != null && !title.trim().isEmpty()) {
                // 제목 정리 및 길이 제한
                title = cleanTitle(title);
                log.info("제목 추출 성공: url={}, title={}", url, title);
                return title;
            } else {
                log.warn("제목을 찾을 수 없음: url={}", url);
                return generateFallbackTitle(url);
            }

        } catch (SocketTimeoutException e) {
            log.warn("제목 추출 타임아웃: url={}, timeout={}ms", url, TIMEOUT_MS);
            return generateFallbackTitle(url);

        } catch (UnknownHostException e) {
            log.warn("호스트를 찾을 수 없음: url={}, error={}", url, e.getMessage());
            return generateFallbackTitle(url);

        } catch (IOException e) {
            log.warn("제목 추출 중 IO 오류: url={}, error={}", url, e.getMessage());
            return generateFallbackTitle(url);

        } catch (Exception e) {
            log.error("제목 추출 중 예상치 못한 오류: url={}, error={}", url, e.getMessage(), e);
            return generateFallbackTitle(url);
        }
    }

    /**
     * Document에서 제목을 추출합니다 (우선순위별)
     */
    private String extractTitleFromDocument(Document document) {
        // 1순위: Open Graph title (SNS 공유용 제목)
        String ogTitle = document.select("meta[property=og:title]").attr("content");
        if (isValidTitle(ogTitle)) {
            log.debug("Open Graph 제목 발견: {}", ogTitle);
            return ogTitle;
        }

        // 2순위: Twitter card title
        String twitterTitle = document.select("meta[name=twitter:title]").attr("content");
        if (isValidTitle(twitterTitle)) {
            log.debug("Twitter 제목 발견: {}", twitterTitle);
            return twitterTitle;
        }

        // 3순위: HTML title 태그
        String htmlTitle = document.title();
        if (isValidTitle(htmlTitle)) {
            log.debug("HTML 제목 발견: {}", htmlTitle);
            return htmlTitle;
        }

        // 4순위: h1 태그 (주로 기사 제목)
        String h1Title = document.select("h1").first() != null ?
                document.select("h1").first().text() : null;
        if (isValidTitle(h1Title)) {
            log.debug("H1 제목 발견: {}", h1Title);
            return h1Title;
        }

        return null;
    }

    /**
     * URL 유효성 검사
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // 기본적인 URL 형식 검사
        String lowerUrl = url.toLowerCase();
        return lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://");
    }

    /**
     * 제목 유효성 검사
     */
    private boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty() && title.length() > 3;
    }

    /**
     * 제목 정리 (불필요한 문자 제거, 길이 제한)
     */
    private String cleanTitle(String title) {
        if (title == null) return null;

        // 앞뒤 공백 제거
        title = title.trim();

        // 여러 공백을 하나로 통일
        title = title.replaceAll("\\s+", " ");

        // 특수 문자 정리 (선택적)
        title = title.replaceAll("[\\r\\n\\t]", " ");

        // 200자 제한 (DB 제약사항)
        if (title.length() > 200) {
            title = title.substring(0, 197) + "...";
        }

        return title;
    }

    /**
     * 제목 추출 실패 시 기본 제목 생성
     */
    private String generateFallbackTitle(String url) {
        try {
            // URL에서 도메인 추출
            String domain = extractDomain(url);
            return domain + " 뉴스 기사";
        } catch (Exception e) {
            return "뉴스 기사";
        }
    }

    /**
     * URL에서 도메인 추출
     */
    private String extractDomain(String url) {
        try {
            if (url.contains("naver.com")) return "네이버";
            if (url.contains("daum.net")) return "다음";
            if (url.contains("chosun.com")) return "조선일보";
            if (url.contains("joongang.co.kr")) return "중앙일보";
            if (url.contains("donga.com")) return "동아일보";
            if (url.contains("hankyung.com")) return "한국경제";
            if (url.contains("mk.co.kr")) return "매일경제";

            // 일반적인 도메인 추출
            String domain = url.replaceAll("https?://", "").split("/")[0];
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            return domain;
        } catch (Exception e) {
            return "온라인";
        }
    }
}