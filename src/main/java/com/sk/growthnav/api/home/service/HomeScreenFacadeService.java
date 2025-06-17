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

        // 4. 홈 화면 응답 생성
        HomeScreenResponse homeScreen = HomeScreenResponse.of(userName, projects, recentConversations);

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