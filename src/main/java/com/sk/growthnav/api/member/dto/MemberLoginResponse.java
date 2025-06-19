// MemberLoginResponse.java
package com.sk.growthnav.api.member.dto;

import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.entity.MemberRole;
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
    MemberRole role;
    Boolean isExpert;
    String message;
    MemberLevel level;

    public static MemberLoginResponse of(Long memberId, String name, String email, MemberRole role, Boolean isExpert, MemberLevel level) {
        return MemberLoginResponse.builder()
                .memberId(memberId)
                .name(name)
                .email(email)
                .role(role)
                .isExpert(isExpert)
                .level(level)
                .message("로그인이 완료되었습니다.")
                .build();
    }
}