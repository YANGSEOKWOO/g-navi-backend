package com.sk.growthnav.api.skill.repository;

import com.sk.growthnav.api.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    /**
     * 프로젝트 ID로 모든 스킬 소회
     * 1개 프로젝트에 여러 스킬이 있을 수 있으므로 List 반환
     *
     * @param projectId
     * @return
     */
    List<Skill> findAllByProjectId(Long projectId);

    /**
     * Project ID로 첫 번째 스킬 조회 (하위 호환성을 위해 유지)
     *
     * @param projectId
     * @return
     */
    Optional<Skill> findByProjectId(Long projectId);

    /**
     * 프로젝트 ID로 스킬 존재 여부 확인
     *
     * @param projectId
     * @return
     */
    boolean existsByProjectId(Long projectId);

    /**
     * 스킬 명으로 검색 (부분 일치)
     *
     * @param skillName
     * @return
     */
    List<Skill> findByNameContainingIgnoreCase(String skillName);
}
