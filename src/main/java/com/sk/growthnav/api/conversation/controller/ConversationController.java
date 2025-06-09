package com.sk.growthnav.api.conversation.controller;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.dto.ConversationStartRequest;
import com.sk.growthnav.api.conversation.dto.ConversationStartResponse;
import com.sk.growthnav.api.conversation.dto.MessageSendRequest;
import com.sk.growthnav.api.conversation.service.ConversationService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")  // 더 직관적인 경로
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 새로운 채팅방 생성
     * POST /api/chatrooms
     * <p>
     * Body: {"memberId": 1, "conversationId": null}  # 새 채팅방
     * Body: {"memberId": 1, "conversationId": "existing_id"}  # 기존 채팅방 재시작
     */
    @PostMapping
    public ApiResponse<ConversationStartResponse> createChatroom(
            @Valid @RequestBody ConversationStartRequest request) {

        log.info("채팅방 생성/재시작 요청: memberId={}, conversationId={}",
                request.getMemberId(), request.getConversationId());

        ConversationStartResponse response = conversationService.startConversation(request);

        log.info("채팅방 생성/재시작 완료: conversationId={}", response.getConversationId());
        return ApiResponse.onSuccess(response);
    }

    /**
     * 기존 채팅방 목록 조회 (현재 사용자)
     * GET /api/chatrooms?memberId=1
     */
    @GetMapping
    public ApiResponse<List<ConversationDocument>> getChatrooms(
            @RequestParam Long memberId) {

        log.info("채팅방 목록 조회: memberId={}", memberId);

        List<ConversationDocument> conversations = conversationService.getConversationsByMember(memberId);

        log.info("채팅방 목록 조회 완료: memberId={}, count={}", memberId, conversations.size());
        return ApiResponse.onSuccess(conversations);
    }

    /**
     * 특정 채팅방 상세 조회 (대화 내용 포함)
     * GET /api/chatrooms/{chatroom_id}
     */
    @GetMapping("/{chatroomId}")
    public ApiResponse<ConversationDocument> getChatroomDetail(@PathVariable String chatroomId) {

        log.info("채팅방 상세 조회: chatroomId={}", chatroomId);

        ConversationDocument conversation = conversationService.getConversationDetail(chatroomId);

        log.info("채팅방 상세 조회 완료: chatroomId={}, messageCount={}",
                chatroomId, conversation.getMessageCount());
        return ApiResponse.onSuccess(conversation);
    }

    /**
     * 채팅방 삭제
     * DELETE /api/chatrooms/{chatroom_id}
     */
    @DeleteMapping("/{chatroomId}")
    public ApiResponse<String> deleteChatroom(@PathVariable String chatroomId) {

        log.info("채팅방 삭제 요청: chatroomId={}", chatroomId);

        conversationService.deleteConversation(chatroomId);

        log.info("채팅방 삭제 완료: chatroomId={}", chatroomId);
        return ApiResponse.onSuccess("채팅방이 삭제되었습니다.");
    }

    /**
     * 메시지 입력 (AI 응답 포함)
     * POST /api/chatrooms/{chatroom_id}/messages
     */
    @PostMapping("/{chatroomId}/messages")
    public ApiResponse<ConversationStartResponse> sendMessage(
            @PathVariable String chatroomId,
            @Valid @RequestBody MessageSendRequest request) {

        // 경로의 chatroomId와 요청 body의 conversationId 일치 검증
        if (!chatroomId.equals(request.getConversationId())) {
            throw new IllegalArgumentException("경로의 채팅방 ID와 요청 데이터의 대화 ID가 일치하지 않습니다.");
        }

        log.info("메시지 전송: chatroomId={}, memberId={}, message={}",
                chatroomId, request.getMemberId(), request.getMessageText());

        ConversationStartResponse response = conversationService.sendMessage(request);

        log.info("메시지 전송 완료: chatroomId={}", chatroomId);
        return ApiResponse.onSuccess(response);
    }

    /**
     * 메시지 목록 조회 (페이징) - 선택사항
     * GET /api/chatrooms/{chatroom_id}/messages?page=0&size=20
     */
    @GetMapping("/{chatroomId}/messages")
    public ApiResponse<ConversationDocument> getChatroomMessages(
            @PathVariable String chatroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("메시지 목록 조회: chatroomId={}, page={}, size={}", chatroomId, page, size);

        // 현재는 전체 메시지 반환, 나중에 페이징 구현 가능
        ConversationDocument conversation = conversationService.getConversationDetail(chatroomId);

        log.info("메시지 목록 조회 완료: chatroomId={}, messageCount={}",
                chatroomId, conversation.getMessageCount());
        return ApiResponse.onSuccess(conversation);
    }
}