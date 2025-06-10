package com.sk.growthnav.api.member.service;

import com.sk.growthnav.api.member.dto.*;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.global.apiPayload.code.FailureCode;
import com.sk.growthnav.global.base.FailureException;
import com.sk.growthnav.global.exception.GeneralException;
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
     * 회원가입
     */
    @Transactional
    public MemberSignupResponse signup(MemberSignupRequest request) {
        log.info("회원가입 시도: email={}, name={}", request.getEmail(), request.getName());

        // 이메일 중복 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            log.warn("이메일 중복: email={}", request.getEmail());
            throw new GeneralException(FailureCode.MEMBER_EMAIL_DUPLICATED);
        }

        // Member 엔티티 생성
        Member member = new Member(
                null, // ID는 자동 생성
                request.getName(),
                request.getPassword(),
                request.getEmail()
        );

        Member savedMember = memberRepository.save(member);
        log.info("회원가입 완료: memberId={}, email={}", savedMember.getId(), savedMember.getEmail());

        return MemberSignupResponse.of(savedMember.getId(), savedMember.getName(), savedMember.getEmail());
    }

    /**
     * 로그인
     */
    public MemberLoginResponse login(MemberLoginRequest request) {
        log.info("로그인 시도: email={}", request.getEmail());

        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일: email={}", request.getEmail());
                    return new GeneralException(FailureCode.MEMBER_NOT_FOUND);
                });

        // 비밀번호 검증 (단순 비교 - 실제로는 BCrypt 등 사용)
        if (!member.getPassword().equals(request.getPassword())) {
            log.warn("비밀번호 불일치: email={}", request.getEmail());
            throw new GeneralException(FailureCode.MEMBER_INVALID_CREDENTIALS);
        }

        log.info("로그인 완료: memberId={}, email={}", member.getId(), member.getEmail());
        return MemberLoginResponse.of(member.getId(), member.getName(), member.getEmail());
    }
}
