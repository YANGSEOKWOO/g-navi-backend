package com.sk.growthnav.api.member.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberSignupResponse {
    String name;
    String email;
    String message;

    public static MemberSignupResponse of(Long memberId, String name, String email) {
        return MemberSignupResponse.builder()
                .name(name)
                .email(email)
                .message("회원가입이 완료되었습니다.")
                .build();
    }
}
