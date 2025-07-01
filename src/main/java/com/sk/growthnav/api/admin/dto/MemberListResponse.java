// 4. 전문가 정보 응답 DTO 수정
package com.sk.growthnav.api.admin.dto;

import com.sk.growthnav.api.member.entity.ExpertiseArea;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.entity.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberListResponse {
    private Long memberId;
    private String name;
    private String email;
    private MemberRole role;
    private Boolean isExpert;
    private MemberLevel level;
    private String joinDate;
    private ExpertiseArea expertiseArea;        // 전문 분야 (enum)
    private String expertiseAreaText;           // 전문 분야 (한글 설명)

    public static MemberListResponse from(Member member) {
        String expertiseText = null;
        if (member.getIsExpert() && member.getExpertiseArea() != null) {
            expertiseText = member.getExpertiseArea().getDescription();
        }

        return MemberListResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .isExpert(member.getIsExpert())
                .level(member.getLevel())
                .joinDate(member.getCreatedAt().toLocalDate().toString())
                .expertiseArea(member.getExpertiseArea())
                .expertiseAreaText(expertiseText)
                .build();
    }
}