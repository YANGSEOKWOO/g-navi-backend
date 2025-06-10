package com.sk.growthnav.api.external.service;// src/main/java/com/sk/growthnav/api/external/service/com.sk.growthnav.api.external.service.FastApiService.java


import com.sk.growthnav.api.conversation.dto.FastApiChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FastApiService {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Value("${fastapi.timeout:30000}")
    private int timeoutMs;

    /**
     * 채팅방 생성 또는 로드 (FastAPI: POST /ai/conversations)
     */
    public String createOrLoadChatroom(FastApiChatRequest request) {
        log.info("FastAPI 채팅방 생성/로드 요청: memberId={}, conversationId={}",
                request.getMemberId(), request.getConversationId());

        try {
            // 1. 요청 URL 구성
            String url = fastApiBaseUrl + "/ai/conversations";

            // 2. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            // 3. HTTP 요청 엔티티 생성
            HttpEntity<FastApiChatRequest> requestEntity = new HttpEntity<>(request, headers);

            // 4. FastAPI 호출
            log.debug("FastAPI 채팅방 호출: url={}, request={}", url, request);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 5. 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String botMessage = extractBotMessage(responseBody);

                log.info("FastAPI 채팅방 응답 성공: memberId={}, responseLength={}",
                        request.getMemberId(), botMessage.length());
                return botMessage;
            } else {
                log.warn("FastAPI 채팅방 응답 실패: status={}, body={}",
                        response.getStatusCode(), response.getBody());
                return getDefaultErrorMessage();
            }

        } catch (RestClientException e) {
            log.error("FastAPI 채팅방 호출 중 네트워크 오류: memberId={}, error={}",
                    request.getMemberId(), e.getMessage(), e);
            return getDefaultErrorMessage();
        } catch (Exception e) {
            log.error("FastAPI 채팅방 호출 중 예상치 못한 오류: memberId={}, error={}",
                    request.getMemberId(), e.getMessage(), e);
            return getDefaultErrorMessage();
        }
    }

    /**
     * 메시지 전송 (FastAPI: POST /ai/conversations/{conversation_id}/messages)
     */
    public String sendMessage(String conversationId, String messageText, String memberId) {
        log.info("FastAPI 메시지 전송: conversationId={}, memberId={}, messageLength={}",
                conversationId, memberId, messageText.length());

        try {
            // 1. 요청 URL 구성
            String url = fastApiBaseUrl + "/ai/conversations/" + conversationId + "/messages";

            // 2. 요청 Body 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("conversationId", conversationId);
            requestBody.put("messageText", messageText);
            requestBody.put("memberId", memberId);

            // 3. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            // 4. HTTP 요청 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 5. FastAPI 호출
            log.debug("FastAPI 메시지 호출: url={}, request={}", url, requestBody);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 6. 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String botMessage = extractBotMessage(responseBody);

                log.info("FastAPI 메시지 응답 성공: conversationId={}, responseLength={}",
                        conversationId, botMessage.length());
                return botMessage;
            } else {
                log.warn("FastAPI 메시지 응답 실패: status={}, body={}",
                        response.getStatusCode(), response.getBody());
                return getDefaultErrorMessage();
            }

        } catch (RestClientException e) {
            log.error("FastAPI 메시지 호출 중 네트워크 오류: conversationId={}, error={}",
                    conversationId, e.getMessage(), e);
            return getDefaultErrorMessage();
        } catch (Exception e) {
            log.error("FastAPI 메시지 호출 중 예상치 못한 오류: conversationId={}, error={}",
                    conversationId, e.getMessage(), e);
            return getDefaultErrorMessage();
        }
    }

    /**
     * FastAPI 응답에서 봇 메시지 추출
     */
    private String extractBotMessage(Map<String, Object> responseBody) {
        // FastAPI 응답 구조: {"conversationId": "string", "botMessage": "...", "timestamp": "..."}

        if (responseBody.containsKey("botMessage")) {
            String botMessage = (String) responseBody.get("botMessage");

            // 응답 로깅 (디버깅용)
            String conversationId = (String) responseBody.get("conversationId");
            String timestamp = (String) responseBody.get("timestamp");
            log.debug("FastAPI 응답 파싱 완료: conversationId={}, timestamp={}, messageLength={}",
                    conversationId, timestamp, botMessage != null ? botMessage.length() : 0);

            return botMessage != null ? botMessage : getDefaultErrorMessage();
        } else {
            log.warn("FastAPI 응답에서 'botMessage' 필드를 찾을 수 없음. 응답 구조: {}", responseBody.keySet());
            log.debug("전체 FastAPI 응답: {}", responseBody);
            return getDefaultErrorMessage();
        }
    }

    /**
     * 기본 에러 메시지
     */
    private String getDefaultErrorMessage() {
        return "죄송합니다. AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
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