package com.sk.growthnav.api.admin.dto;

import com.sk.growthnav.api.member.entity.Member;
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
    private String joinDate;

    public static MemberListResponse from(Member member) {
        return MemberListResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .isExpert(member.getIsExpert())
                .joinDate(member.getCreatedAt().toLocalDate().toString())
                .build();
    }
}