package com.sk.growthnav.api.skill.service;

import com.sk.growthnav.api.skill.dto.SkillInfoDTO;
import com.sk.growthnav.api.skill.entity.Skill;
import com.sk.growthnav.api.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * 프로젝트 모든 스킬 이름 조회
     *
     * @param projectId
     * @return
     */
    public List<String> getSkillNamesByProject(Long projectId) {
        List<Skill> skills = skillRepository.findAllByProjectId(projectId);
        return skills.stream()
                .map(Skill::getName)
                .toList();
    }

    /**
     * 프로젝트의 모든 스킬 DTO 조회
     *
     * @param projectId
     * @return
     */
    public List<SkillInfoDTO> getSkillsByProject(Long projectId) {
        List<Skill> skills = skillRepository.findAllByProjectId(projectId);
        return skills.stream()
                .map(SkillInfoDTO::from)
                .toList();
    }

    /**
     * 스킬 ID로 단일 스킬 조회
     */
    public Skill findById(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("스킬을 찾을 수 없습니다: " + skillId));
    }

    /**
     * 프로젝트에 스킬이 있는지 확인
     */
    public boolean hasSkills(Long projectId) {
        return skillRepository.existsByProjectId(projectId);
    }
}
