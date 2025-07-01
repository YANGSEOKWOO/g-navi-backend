package com.sk.growthnav.api.admin.dto;

import com.sk.growthnav.api.member.entity.ExpertiseArea;
import com.sk.growthnav.api.member.entity.MemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequest {
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "역할은 필수입니다.")
    private MemberRole newRole;

    // 🔥 NEW: EXPERT로 변경할 때만 필수
    private ExpertiseArea expertiseArea;

    // 역할이 EXPERT인지 확인하는 편의 메서드
    public boolean isExpertRole() {
        return newRole == MemberRole.EXPERT;
    }

    // EXPERT 역할인데 전문 분야가 없으면 유효하지 않음
    public boolean isValid() {
        if (isExpertRole()) {
            return expertiseArea != null;
        }
        return true; // USER, ADMIN은 전문 분야 불필요
    }
}