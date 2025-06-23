package com.sk.growthnav.api.news.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsThumbnailService {

    @Value("${app.storage.pvc.path:/mnt/gnavi}")
    private String pvcBasePath;

    @Value("$https://sk-gnavi4.skala25a.project.skala-ai.com}")
    private String baseUrl;

    private static final String THUMBNAIL_DIR = "thumbnails";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 10000;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 뉴스 URL에서 썸네일 이미지를 추출하고 PVC에 저장
     */
    public ThumbnailResult extractAndSaveThumbnail(String newsUrl, Long newsId) {
        log.info("썸네일 추출 시작: newsUrl={}, newsId={}", newsUrl, newsId);

        try {
            // 1. 웹페이지에서 이미지 URL 추출
            String imageUrl = extractImageUrl(newsUrl);
            if (imageUrl == null || imageUrl.isEmpty()) {
                log.warn("이미지 URL 추출 실패: newsUrl={}", newsUrl);
                return ThumbnailResult.failed("이미지 URL을 찾을 수 없습니다.");
            }

            // 2. 이미지 다운로드 및 저장
            String savedPath = downloadAndSaveImage(imageUrl, newsId);
            if (savedPath == null) {
                log.warn("이미지 저장 실패: imageUrl={}", imageUrl);
                return ThumbnailResult.failed("이미지 저장에 실패했습니다.");
            }

            // 3. 접근 가능한 URL 생성
            String accessUrl = generateAccessUrl(savedPath);

            log.info("썸네일 추출 완료: newsId={}, savedPath={}, accessUrl={}",
                    newsId, savedPath, accessUrl);

            return ThumbnailResult.success(savedPath, accessUrl);

        } catch (Exception e) {
            log.error("썸네일 추출 중 오류: newsUrl={}, newsId={}, error={}",
                    newsUrl, newsId, e.getMessage(), e);
            return ThumbnailResult.failed("썸네일 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 웹페이지에서 대표 이미지 URL 추출
     */
    private String extractImageUrl(String newsUrl) {
        try {
            Document document = Jsoup.connect(newsUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            // 1순위: Open Graph 이미지
            String ogImage = document.select("meta[property=og:image]").attr("content");
            if (isValidImageUrl(ogImage)) {
                return makeAbsoluteUrl(ogImage, newsUrl);
            }

            // 2순위: Twitter Card 이미지
            String twitterImage = document.select("meta[name=twitter:image]").attr("content");
            if (isValidImageUrl(twitterImage)) {
                return makeAbsoluteUrl(twitterImage, newsUrl);
            }

            // 3순위: 네이버 뉴스 특화
            if (newsUrl.contains("news.naver.com")) {
                String naverImage = extractNaverNewsImage(document);
                if (isValidImageUrl(naverImage)) {
                    return makeAbsoluteUrl(naverImage, newsUrl);
                }
            }

            // 4순위: 첫 번째 큰 이미지
            String firstImage = document.select("img[src]")
                    .stream()
                    .map(img -> img.attr("src"))
                    .filter(this::isValidImageUrl)
                    .findFirst()
                    .orElse(null);

            if (firstImage != null) {
                return makeAbsoluteUrl(firstImage, newsUrl);
            }

            return null;

        } catch (Exception e) {
            log.warn("이미지 URL 추출 실패: newsUrl={}, error={}", newsUrl, e.getMessage());
            return null;
        }
    }

    /**
     * 네이버 뉴스 특화 이미지 추출
     */
    private String extractNaverNewsImage(Document document) {
        // 네이버 뉴스 기사 이미지 셀렉터들
        String[] selectors = {
                ".end-photo img",
                ".article_body img",
                ".img_desc img",
                ".photo img"
        };

        for (String selector : selectors) {
            String imageSrc = document.select(selector).attr("src");
            if (isValidImageUrl(imageSrc)) {
                return imageSrc;
            }
        }
        return null;
    }

    /**
     * 이미지 다운로드 및 PVC에 저장
     */
    private String downloadAndSaveImage(String imageUrl, Long newsId) {
        try {
            // 1. PVC 썸네일 디렉토리 생성
            Path thumbnailDir = Paths.get(pvcBasePath, THUMBNAIL_DIR);
            Files.createDirectories(thumbnailDir);

            // 2. 파일명 생성 (newsId + UUID + 확장자)
            String fileExtension = getFileExtension(imageUrl);
            String fileName = String.format("news_%d_%s%s", newsId, UUID.randomUUID().toString().substring(0, 8), fileExtension);
            Path filePath = thumbnailDir.resolve(fileName);

            // 3. 이미지 다운로드
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            // 4. 파일 크기 체크
            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_FILE_SIZE) {
                log.warn("파일 크기 초과: imageUrl={}, size={}", imageUrl, contentLength);
                return null;
            }

            // 5. 파일 저장
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 6. 저장 확인
            if (Files.exists(filePath) && Files.size(filePath) > 0) {
                String relativePath = THUMBNAIL_DIR + "/" + fileName;
                log.info("이미지 저장 완료: filePath={}, size={}", filePath, Files.size(filePath));
                return relativePath;
            } else {
                log.warn("이미지 저장 실패: 파일이 생성되지 않음");
                return null;
            }

        } catch (Exception e) {
            log.error("이미지 다운로드 실패: imageUrl={}, error={}", imageUrl, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 접근 가능한 URL 생성
     */
    private String generateAccessUrl(String relativePath) {
        return baseUrl + "/files/" + relativePath;  // /api/files에서 /files로 변경
    }

    /**
     * 이미지 URL 유효성 검사
     */
    private boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }

        String lowerUrl = imageUrl.toLowerCase();
        return (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://") || lowerUrl.startsWith("//")) &&
                (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") || lowerUrl.contains(".png") ||
                        lowerUrl.contains(".gif") || lowerUrl.contains(".webp"));
    }

    /**
     * 상대 URL을 절대 URL로 변환
     */
    private String makeAbsoluteUrl(String imageUrl, String baseUrl) {
        if (imageUrl.startsWith("//")) {
            return "https:" + imageUrl;
        } else if (imageUrl.startsWith("/")) {
            try {
                URL url = new URL(baseUrl);
                return url.getProtocol() + "://" + url.getHost() + imageUrl;
            } catch (Exception e) {
                return imageUrl;
            }
        }
        return imageUrl;
    }

    /**
     * URL에서 파일 확장자 추출
     */
    private String getFileExtension(String imageUrl) {
        try {
            String lowerUrl = imageUrl.toLowerCase();
            if (lowerUrl.contains(".jpg")) return ".jpg";
            if (lowerUrl.contains(".jpeg")) return ".jpeg";
            if (lowerUrl.contains(".png")) return ".png";
            if (lowerUrl.contains(".gif")) return ".gif";
            if (lowerUrl.contains(".webp")) return ".webp";
            return ".jpg"; // 기본값
        } catch (Exception e) {
            return ".jpg";
        }
    }

    /**
     * 썸네일 결과 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class ThumbnailResult {
        private final boolean success;
        private final String filePath;
        private final String accessUrl;
        private final String errorMessage;

        public static ThumbnailResult success(String filePath, String accessUrl) {
            return new ThumbnailResult(true, filePath, accessUrl, null);
        }

        public static ThumbnailResult failed(String errorMessage) {
            return new ThumbnailResult(false, null, null, errorMessage);
        }
    }
}