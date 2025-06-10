package com.sk.growthnav.api.skill.service;

import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.skill.dto.SkillInfoDTO;
import com.sk.growthnav.api.skill.entity.Skill;
import com.sk.growthnav.api.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * 프로젝트에 여러 스킬 생성
     */
    @Transactional
    public List<String> createSkills(Project project, List<String> skillNames) {
        log.info("스킬 생성 시작: projectId={}, skillCount={}", project.getId(), skillNames.size());

        List<Skill> skills = skillNames.stream()
                .distinct()
                .filter(name -> name != null && !name.trim().isEmpty())
                .map(name -> Skill.builder()
                        .name(name.trim())
                        .project(project)
                        .build())
                .collect(Collectors.toList());
        List<Skill> savedSkills = skillRepository.saveAll(skills);
        log.info("스킬 생성 완료: projectId={}, createdCount={}", project.getId(), savedSkills.size());

        return savedSkills.stream()
                .map(Skill::getName)
                .collect(Collectors.toList());

    }

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

    /**
     * 특정 스킬 삭제
     */
    @Transactional
    public void deleteSkill(Long skillId) {
        log.info("스킬 삭제: skillId={}", skillId);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스킬입니다: " + skillId));

        skillRepository.delete(skill);
        log.info("스킬 삭제 완료: skillId={}, skillName={}", skillId, skill.getName());
    }

    /**
     * 스킬 이름으로 검색
     */
    public List<Skill> searchSkillsByName(String skillName) {
        log.debug("스킬 검색: skillName={}", skillName);
        return skillRepository.findByNameContainingIgnoreCase(skillName);
    }

    /**
     * 프로젝트에 스킬 추가 (기존 프로젝트에 스킬 추가할 때 사용)
     */
    @Transactional
    public String addSkillToProject(Project project, String skillName) {
        log.info("프로젝트에 스킬 추가: projectId={}, skillName={}", project.getId(), skillName);

        // 이미 존재하는 스킬인지 확인
        List<Skill> existingSkills = skillRepository.findAllByProjectId(project.getId());
        boolean skillExists = existingSkills.stream()
                .anyMatch(skill -> skill.getName().equalsIgnoreCase(skillName.trim()));

        if (skillExists) {
            log.warn("이미 존재하는 스킬: projectId={}, skillName={}", project.getId(), skillName);
            throw new IllegalArgumentException("이미 존재하는 스킬입니다: " + skillName);
        }

        Skill newSkill = Skill.builder()
                .name(skillName.trim())
                .project(project)
                .build();

        Skill savedSkill = skillRepository.save(newSkill);
        log.info("스킬 추가 완료: skillId={}, skillName={}", savedSkill.getId(), savedSkill.getName());

        return savedSkill.getName();
    }

    /**
     * 프로젝트의 모든 스킬 삭제
     */
    @Transactional
    public void deleteSkillsByProject(Long projectId) {
        log.info("프로젝트 스킬 삭제: projectId={}", projectId);

        List<Skill> skills = skillRepository.findAllByProjectId(projectId);
        skillRepository.deleteAll(skills);

        log.info("프로젝트 스킬 삭제 완료: projectId={}, deletedCount={}", projectId, skills.size());
    }
}
