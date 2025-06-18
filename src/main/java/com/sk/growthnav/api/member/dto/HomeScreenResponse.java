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

        private static String generateChatTitle(ConversationDocument conversation) {
            if (conversation == null || conversation.getMessages().isEmpty()) {
                return "새로운 대화";
            }

            // 1. 첫 번째 사용자 메시지를 제목으로 사용 (가장 자연스러움)
            ConversationDocument.MessageDocument firstUserMessage = conversation.getMessages().stream()
                    .filter(msg -> msg.getSenderType() == SenderType.USER)
                    .findFirst()
                    .orElse(null);

            if (firstUserMessage != null) {
                String messageText = firstUserMessage.getMessageText();
                if (messageText != null && !messageText.trim().isEmpty()) {
                    // 제목 길이 제한 및 정리
                    String title = messageText.trim();

                    // 30자로 제한하고 말줄임표 추가
                    if (title.length() > 30) {
                        title = title.substring(0, 27) + "...";
                    }

                    // 줄바꿈 제거
                    title = title.replaceAll("[\\r\\n\\t]", " ");
                    title = title.replaceAll("\\s+", " ");

                    return title;
                }
            }

            // 2. 첫 번째 메시지가 없으면 마지막 사용자 메시지 사용
            ConversationDocument.MessageDocument lastUserMessage = conversation.getLastUserMessage();
            if (lastUserMessage != null) {
                String messageText = lastUserMessage.getMessageText();
                if (messageText != null && !messageText.trim().isEmpty()) {
                    String title = messageText.trim();
                    if (title.length() > 30) {
                        title = title.substring(0, 27) + "...";
                    }
                    title = title.replaceAll("[\\r\\n\\t]", " ");
                    title = title.replaceAll("\\s+", " ");
                    return title;
                }
            }

            // 3. 사용자 메시지가 없으면 conversationId 기반 (기존 로직)
            String id = conversation.getId();
            if (id != null && id.length() > 8) {
                return "대화 " + id.substring(id.length() - 4);
            }

            // 4. 최종 폴백
            return "Growth Navigator 상담";
        }

        private static String generateSmartChatTitle(ConversationDocument conversation) {
            if (conversation == null || conversation.getMessages().isEmpty()) {
                return "새로운 대화";
            }

            String firstUserMessage = conversation.getMessages().stream()
                    .filter(msg -> msg.getSenderType() == SenderType.USER)
                    .findFirst()
                    .map(ConversationDocument.MessageDocument::getMessageText)
                    .orElse("");

            if (firstUserMessage.isEmpty()) {
                return generateChatTitle(conversation); // 기본 로직으로 폴백
            }

            // 키워드 기반 제목 생성
            String message = firstUserMessage.toLowerCase();

            if (message.contains("프로젝트") || message.contains("project")) {
                return "프로젝트 상담";
            } else if (message.contains("커리어") || message.contains("경력") || message.contains("career")) {
                return "커리어 상담";
            } else if (message.contains("스킬") || message.contains("기술") || message.contains("skill")) {
                return "기술 상담";
            } else if (message.contains("성장") || message.contains("발전")) {
                return "성장 상담";
            } else if (message.contains("이직") || message.contains("취업")) {
                return "취업 상담";
            } else {
                // 키워드가 없으면 첫 30자 사용
                String title = firstUserMessage.trim();
                if (title.length() > 30) {
                    title = title.substring(0, 27) + "...";
                }
                return title.replaceAll("[\\r\\n\\t]", " ").replaceAll("\\s+", " ");
            }
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
