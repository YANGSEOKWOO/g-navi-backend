package com.sk.growthnav.api.member.dto;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HomeScreenResponse {

    String userName;
    List<String> skills;
    List<String> projectNames;

    RecentChat recentChat;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RecentChat {
        String conversationId;
        String title;           // "대화 1", "Growth Navigator 상담" 등
        LocalDateTime lastUpdated;
        boolean hasMessages;    // 메시지가 있는지 여부

        public static RecentChat from(ConversationDocument conversation) {
            if (conversation == null) {
                return RecentChat.builder()
                        .conversationId(null)
                        .title("새로운 대화 시작")
                        .lastUpdated(null)
                        .hasMessages(false)
                        .build();
            }

            // conversationId 기반으로 제목 생성
            String title = generateChatTitle(conversation);

            return RecentChat.builder()
                    .conversationId(conversation.getId())
                    .title(title)
                    .lastUpdated(conversation.getUpdatedAt())
                    .hasMessages(!conversation.isEmpty())
                    .build();
        }

        private static String generateChatTitle(ConversationDocument conversation) {
            // conversationId의 뒷부분을 사용해서 간단한 제목 생성
            String id = conversation.getId();
            if (id != null && id.length() > 8) {
                return "대화 " + id.substring(id.length() - 4); // 마지막 4자리
            }
            return "Growth Navigator 상담";
        }
    }

    // 정적 팩토리 메서드
    public static HomeScreenResponse of(
            String userName,
            List<ProjectInfoDTO> projects,
            ConversationDocument recentConversation) {

        // 1. 프로젝트 이름 목록 추출
        List<String> projectNames = projects.stream()
                .map(ProjectInfoDTO::getProjectName)
                .collect(Collectors.toList());

        // 2. 중복 제거된 스킬 목록 추출
        Set<String> uniqueSkills = projects.stream()
                .filter(project -> project.getSkills() != null)
                .flatMap(project -> project.getSkills().stream())
                .collect(Collectors.toSet());

        List<String> skills = uniqueSkills.stream()
                .sorted() // 알파벳 순으로 정렬
                .collect(Collectors.toList());

        // 3. 최근 대화 정보
        RecentChat recentChat = RecentChat.from(recentConversation);

        return HomeScreenResponse.builder()
                .userName(userName)
                .skills(skills)
                .projectNames(projectNames)
                .recentChat(recentChat)
                .build();
    }
}
