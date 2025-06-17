package com.sk.growthnav.api.admin.dto;

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
}