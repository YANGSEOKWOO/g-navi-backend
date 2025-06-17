package com.sk.growthnav.api.admin.service;

import com.sk.growthnav.api.admin.dto.MemberListResponse;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberRole;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.global.apiPayload.code.FailureCode;
import com.sk.growthnav.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminService {

    private final MemberRepository memberRepository;

    public List<MemberListResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(MemberListResponse::from)
                .toList();
    }

    @Transactional
    public void changeMemberRole(Long memberId, MemberRole newRole) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(FailureCode.MEMBER_NOT_FOUND));

        member.changeRole(newRole);
        memberRepository.save(member);

        log.info("회원 역할 변경: memberId={}, newRole={}, isExpert={}",
                memberId, newRole, member.getIsExpert());
    }
}