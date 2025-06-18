package com.sk.growthnav.api.member.dto;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.global.document.SenderType;
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

    List<RecentChat> recentChats;

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
        int messageCount; // 메시지 개수

        public static RecentChat from(ConversationDocument conversation) {
            if (conversation == null) {
                return RecentChat.builder()
                        .conversationId(null)
                        .title("새로운 대화 시작")
                        .lastUpdated(null)
                        .hasMessages(false)
                        .messageCount(0)
                        .build();
            }

            // conversationId 기반으로 제목 생성
            String title = generateChatTitle(conversation);

            return RecentChat.builder()
                    .conversationId(conversation.getId())
                    .title(title)
                    .lastUpdated(conversation.getUpdatedAt())
                    .hasMessages(!conversation.isEmpty())
                    .messageCount(conversation.getMessageCount())
                    .build();
        }

        /**
         * 대화 제목 생성 - BOT 인사말 이후 첫 번째 사용자 메시지 기반
         */
        private static String generateChatTitle(ConversationDocument conversation) {
            if (conversation == null || conversation.getMessages().isEmpty()) {
                return "새로운 대화";
            }

            // 1. 첫 번째 실제 사용자 질문을 찾기 (BOT 인사말 이후의 첫 USER 메시지)
            List<ConversationDocument.MessageDocument> messages = conversation.getMessages();

            ConversationDocument.MessageDocument firstUserMessage = null;

            // BOT 메시지 다음에 오는 첫 번째 USER 메시지 찾기
            for (int i = 0; i < messages.size(); i++) {
                ConversationDocument.MessageDocument message = messages.get(i);

                if (message.getSenderType() == SenderType.USER) {
                    firstUserMessage = message;
                    break; // 첫 번째 사용자 메시지를 찾으면 바로 사용
                }
            }

            if (firstUserMessage != null) {
                String messageText = firstUserMessage.getMessageText();
                if (messageText != null && !messageText.trim().isEmpty()) {
                    // 제목 길이 제한 및 정리
                    String title = messageText.trim();

                    // 30자로 제한하고 말줄임표 추가
                    if (title.length() > 30) {
                        title = title.substring(0, 27) + "...";
                    }

                    // 줄바꿈 및 특수문자 정리
                    title = title.replaceAll("[\\r\\n\\t]", " ");
                    title = title.replaceAll("\\s+", " ");

                    return title;
                }
            }

            // 2. 사용자 메시지가 없으면 대화 상태에 따른 제목
            if (messages.size() == 1 && messages.get(0).getSenderType() == SenderType.BOT) {
                return "새로운 상담"; // BOT 인사말만 있는 경우
            }

            // 3. conversationId 기반 제목 (폴백)
            String id = conversation.getId();
            if (id != null && id.length() > 8) {
                return "대화 " + id.substring(id.length() - 4);
            }

            // 4. 최종 폴백
            return "Growth Navigator 상담";
        }
    }

    // 정적 팩토리 메서드 - 리스트로 변경
    public static HomeScreenResponse of(
            String userName,
            List<ProjectInfoDTO> projects,
            List<ConversationDocument> recentConversations) {

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

        // 3. 최근 대화 목록 변환
        List<RecentChat> recentChats = recentConversations.stream()
                .map(RecentChat::from)
                .collect(Collectors.toList());

        return HomeScreenResponse.builder()
                .userName(userName)
                .skills(skills)
                .projectNames(projectNames)
                .recentChats(recentChats) // 변경된 필드명
                .build();
    }
}