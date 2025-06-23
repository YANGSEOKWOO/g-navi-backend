package com.sk.growthnav.api.member.dto;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.global.document.SenderType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class HomeScreenResponse {

    String userName;
    MemberLevel level;
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
            log.info("=== generateChatTitle 시작 ===");
            log.info("대화 ID: {}", conversation.getId());

            if (conversation == null || conversation.getMessages().isEmpty()) {
                log.info("대화가 null이거나 메시지가 없음");
                return "새로운 대화";
            }

            List<ConversationDocument.MessageDocument> messages = conversation.getMessages();
            log.info("메시지 총 개수: {}", messages.size());

            // 모든 메시지를 순회하면서 첫 번째 사용자 메시지 찾기
            for (int i = 0; i < messages.size(); i++) {
                ConversationDocument.MessageDocument message = messages.get(i);
                log.info("메시지 {}: 타입={}, 내용={}",
                        i, message.getSenderType(),
                        message.getMessageText() != null ?
                                (message.getMessageText().length() > 30 ?
                                        message.getMessageText().substring(0, 30) + "..." :
                                        message.getMessageText()) : "null");

                // 사용자 메시지인지 확인
                if (message.getSenderType() == SenderType.USER) {
                    String messageText = message.getMessageText();
                    log.info("사용자 메시지 발견: {}", messageText);

                    if (messageText != null && !messageText.trim().isEmpty()) {
                        // 제목 생성
                        String title = cleanAndShortenTitle(messageText);
                        log.info("생성된 제목: '{}'", title);
                        return title;
                    }
                }
            }

            // 사용자 메시지가 없는 경우
            log.info("사용자 메시지를 찾을 수 없음");

            // BOT 메시지만 있는 경우
            if (messages.size() == 1 && messages.get(0).getSenderType() == SenderType.BOT) {
                log.info("BOT 인사말만 있음");
                return "새로운 상담";
            }

            // conversationId 기반 제목 (폴백)
            String id = conversation.getId();
            if (id != null && id.length() >= 4) {
                String fallbackTitle = "대화 " + id.substring(Math.max(0, id.length() - 4));
                log.info("폴백 제목 사용: '{}'", fallbackTitle);
                return fallbackTitle;
            }

            // 최종 폴백
            log.info("최종 폴백 제목 사용");
            return "Growth Navigator 상담";
        }

        /**
         * 제목 정리 및 단축
         */
        private static String cleanAndShortenTitle(String text) {
            if (text == null || text.trim().isEmpty()) {
                return "빈 메시지";
            }

            // 기본 정리
            String cleaned = text.trim()
                    .replaceAll("[\\r\\n\\t]+", " ")  // 줄바꿈을 공백으로
                    .replaceAll("\\s+", " ");         // 연속 공백을 하나로

            // 길이 제한
            if (cleaned.length() > 30) {
                cleaned = cleaned.substring(0, 27) + "...";
            }

            return cleaned;
        }
    }

    // 정적 팩토리 메서드 - 리스트로 변경
    public static HomeScreenResponse of(
            String userName,
            MemberLevel level,
            List<ProjectInfoDTO> projects,
            List<ConversationDocument> recentConversations) {

        log.info("HomeScreenResponse 생성 시작");

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
        log.info("대화 목록을 RecentChat으로 변환 시작, 대화 수: {}", recentConversations.size());
        List<RecentChat> recentChats = recentConversations.stream()
                .map(conversation -> {
                    log.info("대화 변환 중: ID={}", conversation.getId());
                    return RecentChat.from(conversation);
                })
                .collect(Collectors.toList());

        log.info("HomeScreenResponse 생성 완료");
        return HomeScreenResponse.builder()
                .userName(userName)
                .level(level)
                .skills(skills)
                .projectNames(projectNames)
                .recentChats(recentChats)
                .build();
    }
}