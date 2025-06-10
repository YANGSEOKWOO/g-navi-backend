package com.sk.growthnav.api.conversation.controller;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.dto.ConversationStartRequest;
import com.sk.growthnav.api.conversation.dto.ConversationStartResponse;
import com.sk.growthnav.api.conversation.dto.MessageSendRequest;
import com.sk.growthnav.api.conversation.service.ConversationService;
import com.sk.growthnav.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")  // 더 직관적인 경로
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 새로운 채팅방 생성
     * POST /api/conversations
     * <p>
     * Body: {"memberId": 1, "conversationId": null}  # 새 채팅방
     * Body: {"memberId": 1, "conversationId": "existing_id"}  # 기존 채팅방 재시작
     */
    @Operation(
            summary = "새로운 채팅방 생성 또는 기존 채팅방 재시작",
            description = """
                    새로운 대화를 시작하거나 기존 대화를 이어갑니다.
                    
                    **새 대화 시작:**
                    - conversationId를 null 또는 빈 문자열로 전송
                    - 사용자 정보를 기반으로 AI가 첫 인사말 생성
                    
                    **기존 대화 이어가기:**
                    - 유효한 conversationId 전송
                    - 기존 대화 맥락을 유지하여 AI 응답 생성
                    
                    **AI 응답 생성 과정:**
                    1. 사용자 프로젝트 및 스킬 정보 수집
                    2. FastAPI로 컨텍스트 데이터 전송
                    3. AI 응답 생성 및 저장
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "대화 시작 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConversationStartRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "새 대화 시작",
                                            value = """
                                                    {
                                                      "memberId": 1,
                                                      "conversationId": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "기존 대화 이어가기",
                                            value = """
                                                    {
                                                      "memberId": 1,
                                                      "conversationId": "conv_abc123def456"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
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
     * GET /api/conersations?memberId=1
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
     * GET /api/conversations/{conversation_id}
     */
    @Operation(
            summary = "특정 채팅방 상세 조회",
            description = """
                    특정 대화의 전체 메시지 내역을 조회합니다.
                    
                    **제공 정보:**
                    - 전체 대화 메시지 (시간순)
                    - 각 메시지의 발신자 정보
                    - 대화 생성/수정 시간
                    
                    **메시지 타입:**
                    - USER: 사용자 메시지
                    - BOT: AI 응답 메시지
                    """
    )
    @GetMapping("/{conversationId}")
    public ApiResponse<ConversationDocument> getChatroomDetail(@PathVariable String conversationId) {

        log.info("채팅방 상세 조회: conversationId={}", conversationId);

        ConversationDocument conversation = conversationService.getConversationDetail(conversationId);

        log.info("채팅방 상세 조회 완료: conversationId={}, messageCount={}",
                conversationId, conversation.getMessageCount());
        return ApiResponse.onSuccess(conversation);
    }

    /**
     * 채팅방 삭제
     * DELETE /api/chatrooms/{chatroom_id}
     */
    @DeleteMapping("/{conversation_id}")
    public ApiResponse<String> deleteChatroom(@PathVariable String conversationId) {

        log.info("채팅방 삭제 요청: conversationId={}", conversationId);

        conversationService.deleteConversation(conversationId);

        log.info("채팅방 삭제 완료: conversationId={}", conversationId);
        return ApiResponse.onSuccess("채팅방이 삭제되었습니다.");
    }

    /**
     * 메시지 입력 (AI 응답 포함)
     * POST /api/conversations/{conversation_id}/messages
     */
    @Operation(
            summary = "메시지 전송 및 AI 응답 받기",
            description = """
                    사용자 메시지를 전송하고 AI 응답을 받습니다.
                    
                    **처리 과정:**
                    1. 사용자 메시지 저장
                    2. FastAPI로 메시지 전송
                    3. AI 응답 생성
                    4. AI 응답 저장
                    5. 응답 반환
                    
                    **참고사항:**
                    - 메시지는 실시간으로 대화에 추가됨
                    - AI 응답은 사용자의 프로젝트/스킬 정보를 고려하여 생성
                    """,
            parameters = {
                    @Parameter(
                            name = "chatroomId",
                            description = "채팅방 ID",
                            example = "conv_abc123def456",
                            required = true
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "메시지 전송 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageSendRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "conversationId": "conv_abc123def456",
                                              "messageText": "Java 개발자로서 어떻게 성장할 수 있을까요?",
                                              "memberId": 1
                                            }
                                            """
                            )
                    )
            )
    )
    @PostMapping("/{conversation_id}/messages")
    public ApiResponse<ConversationStartResponse> sendMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody MessageSendRequest request) {

        // 경로의 chatroomId와 요청 body의 conversationId 일치 검증
        if (!conversationId.equals(request.getConversationId())) {
            throw new IllegalArgumentException("경로의 채팅방 ID와 요청 데이터의 대화 ID가 일치하지 않습니다.");
        }

        log.info("메시지 전송: chatroomId={}, memberId={}, message={}",
                conversationId, request.getMemberId(), request.getMessageText());

        ConversationStartResponse response = conversationService.sendMessage(request);

        log.info("메시지 전송 완료: chatroomId={}", conversationId);
        return ApiResponse.onSuccess(response);
    }

    /**
     * 메시지 목록 조회 (페이징) - 선택사항
     * GET /api/conversations/{conversation_id}/messages?page=0&size=20
     */
    @GetMapping("/{conversation_id}/messages")
    public ApiResponse<ConversationDocument> getChatroomMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("메시지 목록 조회: chatroomId={}, page={}, size={}", conversationId, page, size);

        // 현재는 전체 메시지 반환, 나중에 페이징 구현 가능
        ConversationDocument conversation = conversationService.getConversationDetail(conversationId);

        log.info("메시지 목록 조회 완료: chatroomId={}, messageCount={}",
                conversationId, conversation.getMessageCount());
        return ApiResponse.onSuccess(conversation);
    }
}