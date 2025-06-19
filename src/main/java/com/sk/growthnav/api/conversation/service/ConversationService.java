package com.sk.growthnav.api.conversation.service;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.dto.ConversationStartRequest;
import com.sk.growthnav.api.conversation.dto.ConversationStartResponse;
import com.sk.growthnav.api.conversation.dto.FastApiChatRequest;
import com.sk.growthnav.api.conversation.dto.MessageSendRequest;
import com.sk.growthnav.api.conversation.repository.ConversationRepository;
import com.sk.growthnav.api.external.service.FastApiService;
import com.sk.growthnav.api.member.dto.MemberInfo;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.service.ProjectService;
import com.sk.growthnav.global.document.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MemberService memberService;
    private final ProjectService projectService;
    private final FastApiService fastApiService;
    // private final com.sk.growthnav.api.external.service.FastApiService fastApiService;  // Issue #4에서 구현

    /**
     * 새로운 대화 시작 또는 기존 대화 이어가기
     */
    @Transactional
    public ConversationStartResponse startConversation(ConversationStartRequest request) {
        log.info("대화 시작 요청: memberId={}, conversationId={}",
                request.getMemberId(), request.getConversationId());

        ConversationDocument conversation;
        boolean isNewConversation = isNullOrEmpty(request.getConversationId());

        if (isNewConversation) {
            // 새로운 대화 생성
            conversation = createNewConversation(request.getMemberId());
            log.info("새 대화 생성: conversationId={}", conversation.getId());
        } else {
            // 기존 대화 불러오기
            conversation = findConversationById(request.getConversationId());
            log.info("기존 대화 불러오기: conversationId={}, messageCount={}",
                    conversation.getId(), conversation.getMessageCount());
        }

        // FastAPI에 전송할 데이터 준비
        String botResponse = callFastApiForInitialResponse(conversation, isNewConversation);

        // Bot 응답을 대화에 추가
        conversation.addMessage(SenderType.BOT, botResponse);
        ConversationDocument savedConversation = conversationRepository.save(conversation);

        return ConversationStartResponse.of(savedConversation.getId(), botResponse);
    }

    /**
     * 메시지 전송 및 AI 응답 처리 (카테고리 분석 포함)
     */
    @Transactional
    public ConversationStartResponse sendMessage(MessageSendRequest request) {
        log.info("메시지 전송: conversationId={}, memberId={}, message={}",
                request.getConversationId(), request.getMemberId(), request.getMessageText());

        // 대화 조회
        ConversationDocument conversation = findConversationById(request.getConversationId());

        // 사용자 메시지 추가 (카테고리 자동 분석 포함)
        conversation.addMessage(SenderType.USER, request.getMessageText());
        conversationRepository.save(conversation);

        // FastAPI로 메시지 전송 및 응답 받기
        String botResponse = callFastApiForMessage(conversation, request.getMessageText());

        // Bot 응답 추가
        conversation.addMessage(SenderType.BOT, botResponse);
        ConversationDocument savedConversation = conversationRepository.save(conversation);

        log.info("메시지 전송 완료: conversationId={}, primaryCategory={}",
                savedConversation.getId(), savedConversation.getPrimaryCategory());

        return ConversationStartResponse.of(savedConversation.getId(), botResponse);
    }


    /**
     * 회원의 대화 목록 조회
     */
    public List<ConversationDocument> getConversationsByMember(Long memberId) {
        log.info("회원 대화 목록 조회: memberId={}", memberId);

        List<ConversationDocument> conversations = conversationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        log.info("조회된 대화 수: {}", conversations.size());

        return conversations;
    }

    /**
     * 특정 대화 상세 조회
     */
    public ConversationDocument getConversationDetail(String conversationId) {
        log.info("대화 상세 조회: conversationId={}", conversationId);
        return findConversationById(conversationId);
    }

    /**
     * 새로운 대화 생성
     */
    private ConversationDocument createNewConversation(Long memberId) {
        // 회원 존재 여부 확인
        memberService.findById(memberId);  // 존재하지 않으면 예외 발생

        ConversationDocument conversation = ConversationDocument.builder()
                .memberId(memberId)
                .build();

        return conversationRepository.save(conversation);
    }

    /**
     * 대화 ID로 대화 조회
     */
    private ConversationDocument findConversationById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다: " + conversationId));
    }

    /**
     * FastAPI 초기 응답 요청 (실제 구현)
     */
    private String callFastApiForInitialResponse(ConversationDocument conversation, boolean isNewConversation) {
        try {
            // 1. 사용자 정보 조회
            MemberInfo memberInfo = memberService.getMemberInfo(conversation.getMemberId());
            List<ProjectInfoDTO> projects = projectService.getProjectsByMember(conversation.getMemberId());

            // 2. FastAPI 요청 데이터 구성
            FastApiChatRequest fastApiRequest = FastApiChatRequest.of(memberInfo, conversation, projects);

            // 3. FastAPI 채팅방 생성/로드 호출 (POST /ai/chatroom)
            String botResponse = fastApiService.createOrLoadChatroom(fastApiRequest);

            log.info("FastAPI 초기 응답 완료: memberId={}, isNew={}, responseLength={}",
                    conversation.getMemberId(), isNewConversation, botResponse.length());

            return botResponse;

        } catch (Exception e) {
            log.error("FastAPI 초기 응답 중 오류: conversationId={}, isNew={}, error={}",
                    conversation.getId(), isNewConversation, e.getMessage(), e);

            // 폴백 메시지
            MemberInfo memberInfo = memberService.getMemberInfo(conversation.getMemberId());
            if (isNewConversation) {
                return String.format("안녕하세요 %s님! Growth Navigator에 오신 것을 환영합니다. " +
                                "현재 AI 서비스에 일시적인 문제가 있어 기본 응답을 드리고 있습니다. 무엇을 도와드릴까요?",
                        memberInfo.getName());
            } else {
                return "이전 대화를 이어가겠습니다. 현재 AI 서비스에 일시적인 문제가 있습니다. 어떤 도움이 필요하신가요?";
            }
        }
    }

    /**
     * FastAPI 메시지 응답 요청 (실제 구현)
     */
    private String callFastApiForMessage(ConversationDocument conversation, String userMessage) {
        try {
            // FastAPI 메시지 전송 호출 (POST /ai/chatroom/{conversation_id}/messages)
            String botResponse = fastApiService.sendMessage(
                    conversation.getId(),
                    userMessage,
                    String.valueOf(conversation.getMemberId())
            );

            log.info("FastAPI 메시지 응답 완료: conversationId={}, userMessageLength={}, responseLength={}",
                    conversation.getId(), userMessage.length(), botResponse.length());

            return botResponse;

        } catch (Exception e) {
            log.error("FastAPI 메시지 응답 중 오류: conversationId={}, userMessage={}, error={}",
                    conversation.getId(), userMessage, e.getMessage(), e);

            // 폴백 메시지
            return "죄송합니다. 현재 AI 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * 채팅방 삭제
     */
    @Transactional
    public void deleteConversation(String conversationId) {
        log.info("채팅방 삭제: conversationId={}", conversationId);

        // 대화 존재 여부 확인
        ConversationDocument conversation = findConversationById(conversationId);

        // 삭제 실행
        conversationRepository.delete(conversation);

        log.info("채팅방 삭제 완료: conversationId={}, messageCount={}",
                conversationId, conversation.getMessageCount());
    }

    /**
     * 빈칸도 NUll로 인식
     *
     * @param str
     * @return
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}