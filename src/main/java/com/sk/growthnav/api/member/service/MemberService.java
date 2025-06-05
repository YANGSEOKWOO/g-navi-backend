package com.sk.growthnav.api.member.service;

import com.sk.growthnav.api.member.dto.MemberInfo;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.global.apiPayload.code.FailureCode;
import com.sk.growthnav.global.base.FailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;

    /**
     * 회원 ID로 회원 정보 조회 (읽기 전용)
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FailureException(FailureCode.MEMBER_NOT_FOUND));
    }

    /**
     * 회원 ID로 회원 정보 DTO 조회 (읽기 전용)
     */
    public MemberInfo getMemberInfo(Long memberId) {
        Member member = findById(memberId);
        log.info("회원 정보 조회 완료: memberId={}, name={}", memberId, member.getName());
        return MemberInfo.from(member);
    }

    /**
     * 이메일 중복 확인 (읽기 전용)
     */
    public boolean isEmailExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 회원 가입 (쓰기 작업) - readOnly 재정의
     */
    @Transactional
    public Member createMember(String name, String email, String password) {
        // 이메일 중복 확인
        if (isEmailExists(email)) {
            throw new FailureException(FailureCode.MEMBER_EMAIL_DUPLICATED);
        }

        Member member = new Member(null, name, password, email);
        Member savedMember = memberRepository.save(member);
        log.info("새 회원 생성 완료: memberId={}, email={}", savedMember.getId(), email);
        return savedMember;
    }
}
