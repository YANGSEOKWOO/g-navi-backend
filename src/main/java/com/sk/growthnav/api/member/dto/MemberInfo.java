package com.sk.growthnav.api.member.dto;

import com.sk.growthnav.api.member.entity.Member;
import lombok.*;
import lombok.experimental.FieldDefaults;

// FastAPI 연동용 데이터 구조
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberInfo {
    Long memberId;
    String name;
    String email;

    public static MemberInfo from(Member member) {
        return MemberInfo.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
