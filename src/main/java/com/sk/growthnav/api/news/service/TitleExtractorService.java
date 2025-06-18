package com.sk.growthnav.api.news.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Service
@Slf4j
public class TitleExtractorService {

    private static final int TIMEOUT_MS = 10000; // 10초
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

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

            // 네이버 뉴스 특별 처리
            if (url.contains("news.naver.com")) {
                return extractNaverNewsTitle(url);
            }

            // 일반 웹페이지 처리
            return extractGeneralTitle(url);

        } catch (Exception e) {
            log.error("제목 추출 중 예상치 못한 오류: url={}, error={}", url, e.getMessage(), e);
            return generateFallbackTitle(url);
        }
    }

    /**
     * 네이버 뉴스 특화 제목 추출
     */
    private String extractNaverNewsTitle(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .referrer("https://www.google.com")  // 구글에서 온 것처럼
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("DNT", "1")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .get();

            // 네이버 뉴스 특화 셀렉터들
            String title = tryExtractTitle(document,
                    "#ct > div.media_end_head.go_trans > div.media_end_head_title > h2",  // 네이버 뉴스 제목
                    ".media_end_head_title h2",
                    "#articleTitle",
                    "h1.tts_head",
                    ".article_header h1"
            );

            if (title != null && !title.trim().isEmpty()) {
                title = cleanTitle(title);
                log.info("네이버 뉴스 제목 추출 성공: {}", title);
                return title;
            }

            // 일반적인 방법으로 재시도
            return extractTitleFromDocument(document);

        } catch (IOException e) {
            log.warn("네이버 뉴스 제목 추출 실패: url={}, error={}", url, e.getMessage());
            return "네이버 뉴스 기사";
        }
    }

    /**
     * 일반 웹페이지 제목 추출
     */
    private String extractGeneralTitle(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            String title = extractTitleFromDocument(document);
            if (title != null && !title.trim().isEmpty()) {
                title = cleanTitle(title);
                log.info("제목 추출 성공: url={}, title={}", url, title);
                return title;
            }

        } catch (SocketTimeoutException e) {
            log.warn("제목 추출 타임아웃: url={}", url);
        } catch (UnknownHostException e) {
            log.warn("호스트를 찾을 수 없음: url={}, error={}", url, e.getMessage());
        } catch (IOException e) {
            log.warn("제목 추출 중 IO 오류: url={}, error={}", url, e.getMessage());
        }

        return generateFallbackTitle(url);
    }

    /**
     * 여러 CSS 셀렉터로 제목 추출 시도
     */
    private String tryExtractTitle(Document document, String... selectors) {
        for (String selector : selectors) {
            try {
                Element element = document.select(selector).first();
                if (element != null) {
                    String title = element.text();
                    if (isValidTitle(title)) {
                        return title;
                    }
                }
            } catch (Exception e) {
                // 다음 셀렉터로 계속
            }
        }
        return null;
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

        title = title.trim();

        // 1. 기본 공백 정리
        title = title.replaceAll("\\s+", " ");
        title = title.replaceAll("[\\r\\n\\t]", " ");

        // 2. 따옴표 처리 - JSON 에러 방지
        title = title.replaceAll("[\"'`]", "");  // 큰따옴표, 작은따옴표, 백틱 제거

        // 3. 기타 JSON에 문제가 될 수 있는 문자들 정리
        title = title.replaceAll("\\\\", "");  // 백슬래시 제거
        title = title.replaceAll("[\\x00-\\x1f\\x7f]", "");  // 제어문자 제거

        // 4. 뉴스 사이트별 특화 정리
        title = title.replaceAll("\\s*-\\s*네이버\\s*뉴스$", "");
        title = title.replaceAll("\\s*-\\s*다음\\s*뉴스$", "");
        title = title.replaceAll("\\s*\\|.*$", ""); // | 뒤의 내용 제거
        title = title.replaceAll("\\s*::.*$", ""); // :: 뒤의 내용 제거

        // 5. 길이 제한 (DB 제약사항: 200자)
        if (title.length() > 200) {
            title = title.substring(0, 197) + "...";
        }

        // 6. 최종 공백 정리
        title = title.trim();

        return title;
    }

    /**
     * 제목 추출 실패 시 기본 제목 생성
     */
    private String generateFallbackTitle(String url) {
        try {
            // URL에서 도메인 추출하여 적절한 기본 제목 생성
            if (url.contains("naver.com")) return "네이버 뉴스 기사";
            if (url.contains("daum.net")) return "다음 뉴스 기사";
            if (url.contains("chosun.com")) return "조선일보 기사";
            if (url.contains("joongang.co.kr")) return "중앙일보 기사";
            if (url.contains("donga.com")) return "동아일보 기사";
            if (url.contains("hankyung.com")) return "한국경제 기사";
            if (url.contains("mk.co.kr")) return "매일경제 기사";

            // 일반적인 도메인 추출
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