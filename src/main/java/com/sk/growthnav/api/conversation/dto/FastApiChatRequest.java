package com.sk.growthnav.api.conversation.dto;

// FastAPI 연동용 DTO
// FastAPI에 보낼 데이터 구조

import com.sk.growthnav.api.member.dto.MemberInfo;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FastApiChatRequest {
    String memberId;
    String roomId; // conversationId
    Map<String, Object> userInfo;

    public static FastApiChatRequest of(MemberInfo member, String conversationId, List<ProjectInfoDTO> projects) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", member.getName());
        userInfo.put("projects", projects);

        return FastApiChatRequest.builder()
                .memberId(String.valueOf(member.getMemberId()))
                .roomId(conversationId)
                .userInfo(userInfo)
                .build();
    }
}
