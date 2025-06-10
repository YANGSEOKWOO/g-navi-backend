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

        // 3. 최근 대화 조회 (가장 최근 업데이트된 대화 1개)
        ConversationDocument recentConversation = getRecentConversation(memberId);

        // 4. 홈 화면 응답 생성
        HomeScreenResponse homeScreen = HomeScreenResponse.of(userName, projects, recentConversation);

        log.info("홈 화면 조회 완료: memberId={}, skillCount={}, projectCount={}, hasRecentChat={}",
                memberId, homeScreen.getSkills().size(), homeScreen.getProjectNames().size(),
                homeScreen.getRecentChat().isHasMessages());

        return homeScreen;
    }

    /**
     * 회원의 가장 최근 대화 조회
     */
    private ConversationDocument getRecentConversation(Long memberId) {
        try {
            List<ConversationDocument> conversations = conversationService.getConversationsByMember(memberId);
            return conversations.isEmpty() ? null : conversations.get(0);
        } catch (Exception e) {
            log.warn("최근 대화 조회 중 오류: memberId={}, error={}", memberId, e.getMessage());
            return null;
        }
    }
}