package com.sk.growthnav.global.auth;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.global.apiPayload.code.FailureCode;
import com.sk.growthnav.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final MemberService memberService;

    // 관리자 권한 확인
    public void validateAdminRole(Long memberId) {
        Member member = memberService.findById(memberId);
        if (!member.isAdmin()) {
            throw new GeneralException(FailureCode._FORBIDDEN);
        }
    }

    // Writer 권한 확인 (Admin도 Writer 가능)
    public void validateWriterRole(Long memberId) {
        Member member = memberService.findById(memberId);
        if (!member.isWriter()) {
            throw new GeneralException(FailureCode._FORBIDDEN);
        }
    }

    // 본인 또는 관리자 확인
    public void validateSelfOrAdmin(Long requesterId, Long targetId) {
        if (!requesterId.equals(targetId)) {
            validateAdminRole(requesterId);
        }
    }
}