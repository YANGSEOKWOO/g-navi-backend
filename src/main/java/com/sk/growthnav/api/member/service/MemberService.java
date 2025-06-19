package com.sk.growthnav.api.member.service;

import com.sk.growthnav.api.member.dto.*;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.entity.MemberRole;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.global.apiPayload.code.FailureCode;
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
    // ConversationService 의존성 제거!

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

        // Member 엔티티 생성 (패스워드는 단순 저장 - 실제 운영에서는 암호화 필요)
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(MemberRole.USER)
                .isExpert(false)
                .level(MemberLevel.CL1)
                .build();
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
        return MemberLoginResponse.of(member.getId(), member.getName(), member.getEmail(), member.getRole(), member.getIsExpert(), member.getLevel());
    }

    /**
     * 회원 ID로 조회
     */
    public Member findById(Long memberId) {
        log.debug("회원 조회: memberId={}", memberId);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(FailureCode.MEMBER_NOT_FOUND));
    }

    /**
     * 회원 정보 조회 (FastAPI 연동용)
     */
    public MemberInfo getMemberInfo(Long memberId) {
        log.debug("회원 정보 조회: memberId={}", memberId);
        Member member = findById(memberId);
        return MemberInfo.from(member);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailExists(String email) {
        log.debug("이메일 중복 확인: email={}", email);
        return memberRepository.existsByEmail(email);
    }

    /**
     * 회원 등급 변경
     */
    @Transactional
    public void changeMemberLevel(Long memberId, MemberLevel newLevel) {
        log.info("회원 등급 변경: memberId={}, newLevel={}", memberId, newLevel);

        Member member = findById(memberId);
        MemberLevel oldLevel = member.getLevel();
        member.changeLevel(newLevel);
        memberRepository.save(member);

        log.info("등급 변경 완료: memberId={}, {} -> {}", memberId, oldLevel, newLevel);

    }
}