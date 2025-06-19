package com.sk.growthnav.api.member.dto;

import com.sk.growthnav.api.member.entity.MemberLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LevelChangeRequest {
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "새로운 등급은 필수입니다.")
    private MemberLevel newLevel;
}
