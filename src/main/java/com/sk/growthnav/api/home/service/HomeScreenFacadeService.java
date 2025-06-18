// src/main/java/com/sk/growthnav/api/home/service/HomeScreenFacadeService.java

package com.sk.growthnav.api.home.service;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.service.ConversationService;
import com.sk.growthnav.api.member.dto.HomeScreenResponse;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeScreenFacadeService {

    private final MemberService memberService;
    private final ProjectService projectService;
    private final ConversationService conversationService;

    /**
     * 홈 화면 데이터 조회
     */
    public HomeScreenResponse getHomeScreenData(Long memberId) {
        log.info("홈 화면 데이터 조회: memberId={}", memberId);

        // 1. 회원 정보 조회
        Member member = memberService.findById(memberId);
        String userName = member.getName();

        // 2. 회원의 프로젝트 목록 조회 (스킬 포함)
        List<ProjectInfoDTO> projects = projectService.getProjectsByMember(memberId);

        // 3. 최근 대화 목록 조회 (모든 대화)
        List<ConversationDocument> recentConversations = getRecentConversations(memberId);

        // 디버깅: 대화 내용 상세 로그
        log.info("=== 대화 데이터 디버깅 시작 ===");
        for (ConversationDocument conversation : recentConversations) {
            log.info("대화 ID: {}, 메시지 수: {}", conversation.getId(), conversation.getMessageCount());

            // 각 메시지 출력
            for (int i = 0; i < conversation.getMessages().size(); i++) {
                ConversationDocument.MessageDocument message = conversation.getMessages().get(i);
                String messagePreview = message.getMessageText().length() > 50 ?
                        message.getMessageText().substring(0, 50) + "..." : message.getMessageText();
                log.info("  메시지 {}: [{}] {}", i + 1, message.getSenderType(), messagePreview);
            }
        }
        log.info("=== 대화 데이터 디버깅 종료 ===");

        // 4. 홈 화면 응답 생성
        HomeScreenResponse homeScreen = HomeScreenResponse.of(userName, projects, recentConversations);

        log.info("홈 화면 조회 완료: memberId={}, skillCount={}, projectCount={}, conversationCount={}",
                memberId,
                homeScreen.getSkills() != null ? homeScreen.getSkills().size() : 0,
                homeScreen.getProjectNames() != null ? homeScreen.getProjectNames().size() : 0,
                homeScreen.getRecentChats() != null ? homeScreen.getRecentChats().size() : 0);

        // 생성된 제목들도 로그로 확인
        if (homeScreen.getRecentChats() != null) {
            log.info("=== 생성된 대화 제목들 ===");
            for (HomeScreenResponse.RecentChat chat : homeScreen.getRecentChats()) {
                log.info("대화 ID: {}, 제목: '{}'", chat.getConversationId(), chat.getTitle());
            }
        }

        return homeScreen;
    }

    /**
     * 회원의 모든 최근 대화 조회
     */
    private List<ConversationDocument> getRecentConversations(Long memberId) {
        try {
            List<ConversationDocument> conversations = conversationService.getConversationsByMember(memberId);
            log.debug("최근 대화 조회 성공: memberId={}, conversationCount={}", memberId, conversations.size());
            return conversations;
        } catch (Exception e) {
            log.warn("최근 대화 조회 중 오류: memberId={}, error={}", memberId, e.getMessage());
            return List.of(); // 빈 리스트 반환
        }
    }
}