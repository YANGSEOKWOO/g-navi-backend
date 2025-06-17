package com.sk.growthnav.api.conversation.dto;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.member.dto.MemberInfo;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FastApiChatRequest {
    // FastAPI가 camelCase를 기대하므로 camelCase로 변경
    String memberId;
    String conversationId;
    List<Map<String, Object>> messages; // 기존 대화 메시지들
    Map<String, Object> userInfo;

    public static FastApiChatRequest of(MemberInfo member, ConversationDocument conversation, List<ProjectInfoDTO> projects) {
        // 1. 사용자 정보 구성
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", member.getName());

        // 2. 프로젝트 정보를 FastAPI 형식으로 변환 (camelCase)
        List<Map<String, Object>> projectsFormatted = projects.stream()
                .map(project -> {
                    Map<String, Object> projectMap = new HashMap<>();
                    projectMap.put("projectId", project.getProjectId());
                    projectMap.put("projectName", project.getProjectName());
                    projectMap.put("role", project.getUserRole());
                    projectMap.put("domain", project.getDomain());
                    projectMap.put("scale", project.getProjectScale());
                    projectMap.put("startDate", project.getStartDate());
                    projectMap.put("endDate", project.getEndDate());
                    projectMap.put("skills", project.getSkills());
                    return projectMap;
                })
                .toList();

        userInfo.put("projects", projectsFormatted);

        // 3. 기존 메시지들을 FastAPI 형식으로 변환
        List<Map<String, Object>> messagesFormatted = new ArrayList<>();
        if (conversation != null && !conversation.isEmpty()) {
            messagesFormatted = conversation.getMessages().stream()
                    .map(message -> {
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("sender", message.getSenderType().name());
                        messageMap.put("message", message.getMessageText());
                        messageMap.put("timestamp", message.getTimestamp().toString());
                        return messageMap;
                    })
                    .toList();
        }

        return FastApiChatRequest.builder()
                .memberId(String.valueOf(member.getMemberId()))
                .conversationId(conversation != null ? conversation.getId() : null)
                .messages(messagesFormatted)
                .userInfo(userInfo)
                .build();
    }
}