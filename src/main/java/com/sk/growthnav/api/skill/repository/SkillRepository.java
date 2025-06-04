package com.sk.growthnav.api.skill.repository;

import com.sk.growthnav.api.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByProjectId(Long projectId);
}
