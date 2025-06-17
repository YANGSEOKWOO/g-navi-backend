// src/main/java/com/sk/growthnav/api/member/repository/MemberRepository.java
package com.sk.growthnav.api.member.repository;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 기존 메서드들
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    // 새로 추가: 역할 관련 메서드들

    /**
     * 특정 역할을 가진 사용자가 존재하는지 확인
     */
    boolean existsByRole(MemberRole role);

    /**
     * 특정 역할을 가진 모든 사용자 조회
     */
    List<Member> findByRole(MemberRole role);

    /**
     * 특정 역할을 가진 첫 번째 사용자 조회 (Admin 조회용)
     */
    Optional<Member> findFirstByRole(MemberRole role);

    /**
     * 특정 역할을 가진 사용자 수 조회
     */
    long countByRole(MemberRole role);

    /**
     * 전문가(isExpert=true) 사용자들 조회
     */
    List<Member> findByIsExpertTrue();

    /**
     * 역할과 전문가 여부로 조회 (복합 조건)
     */
    List<Member> findByRoleAndIsExpert(MemberRole role, Boolean isExpert);
}