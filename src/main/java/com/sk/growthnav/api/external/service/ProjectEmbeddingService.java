package com.sk.growthnav.api.external.service;

import com.sk.growthnav.api.external.dto.ProjectEmbeddingRequest;
import com.sk.growthnav.api.external.dto.ProjectEmbeddingResponse;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.skill.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * FastAPI 프로젝트 임베딩 서비스
 * 전문가 역할의 새로운 프로젝트만 FastAPI로 전송하여 임베딩 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectEmbeddingService {

    private final RestTemplate restTemplate;
    private final SkillService skillService;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Value("${fastapi.timeout:30000}")
    private int timeoutMs;

    /**
     * 전문가 역할의 새로운 프로젝트를 FastAPI로 전송하여 임베딩 처리
     */
    public void sendNewProjectToFastApi(Project project, Member member) {
        log.info("전문가 프로젝트 FastAPI 전송: projectId={}, memberId={}, memberRole={}, projectName={}",
                project.getId(), member.getId(), member.getRole(), project.getName());

        try {
            // 전문가 역할 확인
            if (!member.isEXPERT()) {
                log.warn("전문가가 아닌 사용자의 프로젝트 전송 시도: memberId={}, role={}",
                        member.getId(), member.getRole());
                return;
            }

            // 1. 프로젝트의 스킬 정보 조회
            List<String> skills = skillService.getSkillNamesByProject(project.getId());

            // 2. FastAPI 요청 DTO 생성
            ProjectEmbeddingRequest request = ProjectEmbeddingRequest.from(project, member, skills);

            // 3. FastAPI 호출
            ProjectEmbeddingResponse response = callFastApiEmbedding(request);

            // 4. 응답 로깅
            logEmbeddingResult(project, response);

        } catch (Exception e) {
            log.error("전문가 프로젝트 FastAPI 전송 실패: projectId={}, memberId={}, error={}",
                    project.getId(), member.getId(), e.getMessage(), e);
            // 실패해도 메인 로직에는 영향을 주지 않도록 예외를 다시 던지지 않음
        }
    }

    /**
     * FastAPI 임베딩 API 호출
     */
    private ProjectEmbeddingResponse callFastApiEmbedding(ProjectEmbeddingRequest request) {
        try {
            // 1. 요청 URL 구성
            String url = fastApiBaseUrl + "/ai/project-embedding/single";

            // 2. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            // 3. HTTP 요청 엔티티 생성
            HttpEntity<ProjectEmbeddingRequest> requestEntity = new HttpEntity<>(request, headers);

            // 4. FastAPI 호출
            log.debug("FastAPI 전문가 프로젝트 임베딩 호출: url={}, employeeId={}, memberRole={}, projectName={}",
                    url, request.getEmployee_id(), request.getRole(), request.getProject_name());

            ResponseEntity<ProjectEmbeddingResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    ProjectEmbeddingResponse.class
            );

            // 5. 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("FastAPI 프로젝트 임베딩 성공: employeeId={}, projectName={}, documentId={}",
                        request.getEmployee_id(), request.getProject_name(),
                        response.getBody().getDocument_id());
                return response.getBody();
            } else {
                log.warn("FastAPI 프로젝트 임베딩 응답 실패: status={}, body={}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("FastAPI 응답 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("FastAPI 프로젝트 임베딩 호출 중 오류: employeeId={}, projectName={}, error={}",
                    request.getEmployee_id(), request.getProject_name(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 전문가 프로젝트 임베딩 결과 로깅
     */
    private void logEmbeddingResult(Project project, ProjectEmbeddingResponse response) {
        if (response.getEmbedding_success() && response.getStored_in_vectordb()) {
            log.info("✅ 전문가 프로젝트 임베딩 성공: projectId={}, projectName={}, documentId={}",
                    project.getId(), project.getName(), response.getDocument_id());
        } else {
            log.warn("⚠️ 전문가 프로젝트 임베딩 부분 실패: projectId={}, embeddingSuccess={}, storedInVectorDB={}, message={}",
                    project.getId(), response.getEmbedding_success(),
                    response.getStored_in_vectordb(), response.getMessage());
        }
    }

    /**
     * FastAPI 서버 상태 확인
     */
    public boolean isHealthy() {
        try {
            String healthUrl = fastApiBaseUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("FastAPI 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }
}