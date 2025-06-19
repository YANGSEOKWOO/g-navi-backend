package com.sk.growthnav.api.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")  // /api/files에서 /files로 변경
@Slf4j
public class FileController {

    @Value("${app.storage.pvc.path:/app/storage}")
    private String pvcBasePath;

    /**
     * PVC에 저장된 파일 서빙
     * GET /files/thumbnails/news_1_abc123.jpg
     */
    @Operation(
            summary = "파일 서빙",
            description = "PVC에 저장된 파일을 서빙합니다. 주로 뉴스 썸네일 이미지 용도로 사용됩니다.",
            hidden = true  // 일반 사용자용 API가 아니므로 숨김
    )
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "파일 경로", example = "thumbnails/news_1_abc123.jpg")
            @RequestParam String path) {
        try {
            // 1. 보안: path traversal 공격 방지
            if (path.contains("..") || path.contains("~") || path.startsWith("/")) {
                log.warn("잘못된 파일 경로: {}", path);
                return ResponseEntity.badRequest().build();
            }

            // 2. 파일 경로 구성
            Path filePath = Paths.get(pvcBasePath).resolve(path).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 3. 파일 존재 여부 확인
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일을 찾을 수 없음: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 4. MIME 타입 결정
            MediaType mediaType = determineMediaType(path);

            // 5. 응답 헤더 설정
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // 1일 캐시
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // 브라우저에서 직접 표시
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 서빙 중 오류: path={}, error={}", path, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * PathVariable 방식으로도 지원
     * GET /files/thumbnails/news_1_abc123.jpg
     */
    @Operation(
            summary = "썸네일 이미지 서빙",
            description = """
                    뉴스 기사의 썸네일 이미지를 서빙합니다.
                    
                    **사용 예시:**
                    - GET /files/thumbnails/news_1_abc123.jpg
                    - 브라우저에서 직접 접근 가능
                    - 이미지 태그의 src 속성으로 사용
                    
                    **지원 형식:** JPG, PNG, GIF, WebP
                    """,
            tags = {"파일 서빙"}
    )
    @GetMapping("/thumbnails/{fileName}")
    public ResponseEntity<Resource> serveThumbnail(
            @Parameter(description = "썸네일 파일명", example = "news_1_abc123.jpg")
            @PathVariable String fileName) {
        return serveFile("thumbnails/" + fileName);
    }

    /**
     * 파일 확장자에 따른 MIME 타입 결정
     */
    private MediaType determineMediaType(String path) {
        String lowerPath = path.toLowerCase();

        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lowerPath.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerPath.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lowerPath.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}