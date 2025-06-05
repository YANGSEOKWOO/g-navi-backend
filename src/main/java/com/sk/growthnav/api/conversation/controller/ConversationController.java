package com.sk.growthnav.api.conversation.controller;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.dto.ConversationStartRequest;
import com.sk.growthnav.api.conversation.dto.ConversationStartResponse;
import com.sk.growthnav.api.conversation.dto.MessageSendRequest;
import com.sk.growthnav.api.conversation.service.ConversationService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversation", description = "대화 관리 API")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 새로운 대화 시작 또는 기존 대화 이어가기
     * POST /api/conversations/start
     */
    @PostMapping("/start")
    @Operation(summary = "대화 시작", description = "새로운 대화를 시작하거나 기존 대화를 이어갑니다.")
    public ApiResponse<ConversationStartResponse> startConversation(
            @Valid @RequestBody ConversationStartRequest request) {

        log.info("대화 시작 API 호출: memberId={}, conversationId={}",
                request.getMemberId(), request.getConversationId());

        ConversationStartResponse response = conversationService.startConversation(request);

        log.info("대화 시작 완료: conversationId={}", response.getConversationId());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 메시지 전송
     * POST /api/conversations/message
     */
    @PostMapping("/message")
    @Operation(summary = "메시지 전송", description = "대화에 메시지를 전송하고 AI 응답을 받습니다.")
    public ApiResponse<ConversationStartResponse> sendMessage(
            @Valid @RequestBody MessageSendRequest request) {

        log.info("메시지 전송 API 호출: conversationId={}, memberId={}",
                request.getConversationId(), request.getMemberId());

        ConversationStartResponse response = conversationService.sendMessage(request);

        log.info("메시지 전송 완료: conversationId={}", response.getConversationId());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 회원의 대화 목록 조회
     * GET /api/conversations/member/{memberId}
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "대화 목록 조회", description = "특정 회원의 모든 대화 목록을 조회합니다.")
    public ApiResponse<List<ConversationListResponse>> getConversationsByMember(
            @Parameter(description = "회원 ID") @PathVariable Long memberId) {

        log.info("대화 목록 조회 API 호출: memberId={}", memberId);

        List<ConversationDocument> conversations = conversationService.getConversationsByMember(memberId);

        // ConversationDocument -> ConversationListResponse 변환
        List<ConversationListResponse> response = conversations.stream()
                .map(ConversationListResponse::from)
                .toList();

        log.info("대화 목록 조회 완료: memberId={}, count={}", memberId, response.size());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 특정 대화 상세 조회
     * GET /api/conversations/{conversationId}
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "대화 상세 조회", description = "특정 대화의 전체 메시지를 조회합니다.")
    public ApiResponse<ConversationDetailResponse> getConversationDetail(
            @Parameter(description = "대화 ID") @PathVariable String conversationId) {

        log.info("대화 상세 조회 API 호출: conversationId={}", conversationId);

        ConversationDocument conversation = conversationService.getConversationDetail(conversationId);
        ConversationDetailResponse response = ConversationDetailResponse.from(conversation);

        log.info("대화 상세 조회 완료: conversationId={}, messageCount={}",
                conversationId, response.getMessageCount());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 대화 목록 응답 DTO
     */
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ConversationListResponse {
        private String conversationId;
        private Long memberId;
        private String lastMessage;
        private String lastMessageTime;
        private int messageCount;
        private java.time.LocalDateTime createdAt;

        public static ConversationListResponse from(ConversationDocument conversation) {
            ConversationDocument.MessageDocument latestMessage = conversation.getLatestMessage();

            return ConversationListResponse.builder()
                    .conversationId(conversation.getId())
                    .memberId(conversation.getMemberId())
                    .lastMessage(latestMessage != null ? latestMessage.getMessageText() : "")
                    .lastMessageTime(latestMessage != null ?
                            formatTime(latestMessage.getTimestamp()) : "")
                    .messageCount(conversation.getMessageCount())
                    .createdAt(conversation.getCreatedAt())
                    .build();
        }

        private static String formatTime(java.time.LocalDateTime dateTime) {
            // 간단한 시간 포맷팅 (예: "2시간 전", "1일 전")
            java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());

            if (duration.toMinutes() < 60) {
                return duration.toMinutes() + "분 전";
            } else if (duration.toHours() < 24) {
                return duration.toHours() + "시간 전";
            } else {
                return duration.toDays() + "일 전";
            }
        }
    }

    /**
     * 대화 상세 응답 DTO
     */
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ConversationDetailResponse {
        private String conversationId;
        private Long memberId;
        private List<MessageResponse> messages;
        private int messageCount;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public static ConversationDetailResponse from(ConversationDocument conversation) {
            List<MessageResponse> messages = conversation.getMessages().stream()
                    .map(MessageResponse::from)
                    .toList();

            return ConversationDetailResponse.builder()
                    .conversationId(conversation.getId())
                    .memberId(conversation.getMemberId())
                    .messages(messages)
                    .messageCount(conversation.getMessageCount())
                    .createdAt(conversation.getCreatedAt())
                    .updatedAt(conversation.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 메시지 응답 DTO
     */
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class MessageResponse {
        private String senderType;
        private String messageText;
        private java.time.LocalDateTime timestamp;

        public static MessageResponse from(ConversationDocument.MessageDocument message) {
            return MessageResponse.builder()
                    .senderType(message.getSenderType().name())
                    .messageText(message.getMessageText())
                    .timestamp(message.getTimestamp())
                    .build();
        }
    }
}