// MemberLoginResponse.java
package com.sk.growthnav.api.member.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberLoginResponse {
    Long memberId;
    String name;
    String email;
    String message;

    public static MemberLoginResponse of(Long memberId, String name, String email) {
        return MemberLoginResponse.builder()
                .memberId(memberId)
                .name(name)
                .email(email)
                .message("로그인이 완료되었습니다.")
                .build();
    }
}