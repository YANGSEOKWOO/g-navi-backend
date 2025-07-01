package com.sk.growthnav.api.member.repository;

import com.sk.growthnav.api.member.entity.ExpertiseArea;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.entity.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 기존 메서드들
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(MemberRole role);

    List<Member> findByRole(MemberRole role);

    Optional<Member> findFirstByRole(MemberRole role);

    long countByRole(MemberRole role);

    List<Member> findByIsExpertTrue();

    List<Member> findByRoleAndIsExpert(MemberRole role, Boolean isExpert);

    // ===== 새로 추가된 등급 관련 메서드들 =====

    /**
     * 특정 등급을 가진 모든 사용자 조회
     */
    List<Member> findByLevel(MemberLevel level);

    /**
     * 특정 등급을 가진 사용자 수 조회
     */
    long countByLevel(MemberLevel level);

    /**
     * 등급별 사용자 분포 조회 (통계용)
     */
    @Query("SELECT m.level, COUNT(m) FROM Member m GROUP BY m.level")
    List<Object[]> countByLevelGroupBy();

    /**
     * 등급과 역할 조합으로 조회
     */
    List<Member> findByLevelAndRole(MemberLevel level, MemberRole role);

    /**
     * 시니어급(CL3 이상) 사용자 조회
     */
    @Query("SELECT m FROM Member m WHERE m.level IN ('CL3', 'CL4', 'CL5')")
    List<Member> findSeniorMembers();

    /**
     * 주니어급(CL1, CL2) 사용자 조회
     */
    @Query("SELECT m FROM Member m WHERE m.level IN ('CL1', 'CL2')")
    List<Member> findJuniorMembers();


    /**
     * 가장 일반적인 등급 조회 (최빈값)
     */
    @Query("SELECT m.level FROM Member m GROUP BY m.level ORDER BY COUNT(m.level) DESC")
    List<MemberLevel> findMostCommonLevels();

    /**
     * 멘토링 가능한 시니어 조회 (특정 등급보다 높은 등급을 가진 사용자)
     */
    @Query("SELECT m FROM Member m WHERE " +
            "((CASE WHEN :targetLevel = 'CL1' THEN 1 " +
            "       WHEN :targetLevel = 'CL2' THEN 2 " +
            "       WHEN :targetLevel = 'CL3' THEN 3 " +
            "       WHEN :targetLevel = 'CL4' THEN 4 " +
            "       WHEN :targetLevel = 'CL5' THEN 5 END) < " +
            " (CASE WHEN m.level = 'CL1' THEN 1 " +
            "       WHEN m.level = 'CL2' THEN 2 " +
            "       WHEN m.level = 'CL3' THEN 3 " +
            "       WHEN m.level = 'CL4' THEN 4 " +
            "       WHEN m.level = 'CL5' THEN 5 END))")
    List<Member> findPotentialMentors(MemberLevel targetLevel);

    @Query("SELECT m FROM Member m WHERE m.expertiseArea = :area AND m.isExpert = true")
    List<Member> findExpertsByExpertiseArea(@Param("area") ExpertiseArea area);

    @Query("SELECT m FROM Member m WHERE m.isExpert = true ORDER BY m.createdAt DESC")
    List<Member> findAllExperts();
}