package com.sk.growthnav.api.admin.dto;

import com.sk.growthnav.api.member.entity.ExpertiseArea;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExpertAssignmentRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "관리자 ID는 필수입니다.")
    private Long adminId;

    @NotNull(message = "전문 분야는 필수입니다.")
    private ExpertiseArea expertiseArea;
}