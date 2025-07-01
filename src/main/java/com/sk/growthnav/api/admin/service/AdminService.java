package com.sk.growthnav.api.admin.service;

import com.sk.growthnav.api.admin.dto.MemberListResponse;
import com.sk.growthnav.api.admin.dto.RoleChangeRequest;
import com.sk.growthnav.api.member.entity.ExpertiseArea;
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
    public String changeMemberRole(RoleChangeRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new GeneralException(FailureCode.MEMBER_NOT_FOUND));

        MemberRole oldRole = member.getRole();
        ExpertiseArea oldArea = member.getExpertiseArea();

        // 역할 변경 (전문 분야 포함)
        member.changeRole(request.getNewRole(), request.getExpertiseArea());
        memberRepository.save(member);

        // 로그 및 응답 메시지 생성
        String resultMessage = generateRoleChangeMessage(member, oldRole, oldArea, request);

        log.info("역할 변경 완료: memberId={}, name={}, {} → {}",
                member.getId(), member.getName(), oldRole, request.getNewRole());

        return resultMessage;
    }

    private String generateRoleChangeMessage(Member member, MemberRole oldRole,
                                             ExpertiseArea oldArea, RoleChangeRequest request) {
        String baseName = member.getName();
        String oldRoleText = getRoleText(oldRole, oldArea);
        String newRoleText = getRoleText(request.getNewRole(), request.getExpertiseArea());

        return String.format("%s님의 역할이 변경되었습니다. %s → %s",
                baseName, oldRoleText, newRoleText);
    }

    private String getRoleText(MemberRole role, ExpertiseArea area) {
        return switch (role) {
            case USER -> "일반 사용자";
            case ADMIN -> "관리자";
            case EXPERT -> area != null ?
                    String.format("전문가 (%s)", area.getDescription()) : "전문가";
        };
    }

    public List<MemberListResponse> getExpertsByArea(ExpertiseArea area) {
        List<Member> experts = memberRepository.findExpertsByExpertiseArea(area);
        return experts.stream()
                .map(MemberListResponse::from)
                .toList();
    }

    public List<MemberListResponse> getAllExperts() {
        List<Member> experts = memberRepository.findAllExperts();
        return experts.stream()
                .map(MemberListResponse::from)
                .toList();
    }

    // 기존 메서드는 deprecated로 유지 (하위 호환성)
    @Deprecated
    public void changeMemberRole(Long memberId, MemberRole newRole) {
        RoleChangeRequest request = new RoleChangeRequest(memberId, newRole, null);
        changeMemberRole(request);
    }
}