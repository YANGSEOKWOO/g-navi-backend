// 1단계: HomeScreenFacadeService.java 수정 - 디버깅 로그 추가

package com.sk.growthnav.api.home.service;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.service.ConversationService;
import com.sk.growthnav.api.member.dto.HomeScreenResponse;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
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
        MemberLevel level = member.getLevel();

        // 2. 회원의 프로젝트 목록 조회 (스킬 포함)
        List<ProjectInfoDTO> projects = projectService.getProjectsByMember(memberId);

        // 3. 최근 대화 목록 조회 (모든 대화)
        List<ConversationDocument> recentConversations = getRecentConversations(memberId);

        // ===== 추가된 디버깅 코드 =====
        log.info("=== 대화 제목 생성 디버깅 시작 ===");
        log.info("조회된 대화 수: {}", recentConversations.size());

        for (int i = 0; i < recentConversations.size(); i++) {
            ConversationDocument conversation = recentConversations.get(i);
            log.info("대화 {}: ID={}, 메시지 수={}",
                    i + 1, conversation.getId(), conversation.getMessageCount());

            // 각 메시지의 상세 정보 출력
            if (conversation.getMessages() != null) {
                for (int j = 0; j < conversation.getMessages().size(); j++) {
                    ConversationDocument.MessageDocument message = conversation.getMessages().get(j);
                    String messagePreview = message.getMessageText().length() > 50 ?
                            message.getMessageText().substring(0, 50) + "..." : message.getMessageText();
                    log.info("  메시지 {}: [{}] {}",
                            j + 1, message.getSenderType(), messagePreview);
                }
            }
        }
        log.info("=== 대화 제목 생성 디버깅 종료 ===");

        // 4. 홈 화면 응답 생성
        HomeScreenResponse homeScreen = HomeScreenResponse.of(userName, level, projects, recentConversations);

        // ===== 생성된 제목 확인 =====
        log.info("=== 생성된 제목들 확인 ===");
        if (homeScreen.getRecentChats() != null) {
            for (int i = 0; i < homeScreen.getRecentChats().size(); i++) {
                HomeScreenResponse.RecentChat chat = homeScreen.getRecentChats().get(i);
                log.info("제목 {}: ID={}, 제목='{}'",
                        i + 1, chat.getConversationId(), chat.getTitle());
            }
        }

        log.info("홈 화면 조회 완료: memberId={}, skillCount={}, projectCount={}, conversationCount={}",
                memberId,
                homeScreen.getSkills() != null ? homeScreen.getSkills().size() : 0,
                homeScreen.getProjectNames() != null ? homeScreen.getProjectNames().size() : 0,
                homeScreen.getRecentChats() != null ? homeScreen.getRecentChats().size() : 0);

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