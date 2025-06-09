package com.sk.growthnav.api.project.service;

import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.project.repository.ProjectRepository;
import com.sk.growthnav.api.skill.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SkillService skillService;

    /**
     * 회원의 모든 프로젝트 조회
     *
     * @param memberId
     * @return
     */
    public List<Project> findByMemberId(Long memberId) {
        return projectRepository.findByMemberId(memberId);
    }

    /**
     * 회원의 프로젝트 정보를 DTO로 변환하여 조회
     * FastAPI 연동용 데이터 구조
     *
     * @param memberId
     * @return
     */
    public List<ProjectInfoDTO> getProjectsByMember(Long memberId) {
        List<Project> projects = findByMemberId(memberId);

        log.info("회원 {}의 프로젝트 {}개 조회됨.", memberId, projects.size());

        return projects.stream()
                .map(project -> {
                    List<String> skills = skillService.getSkillNamesByProject(project.getId());
                    log.debug("프로젝트 {}의 스킬 {}개: {}", project.getId(), skills.size(), skills);

                    return ProjectInfoDTO.from(project, skills);
                })
                .toList();
    }

    /**
     * 프로젝트 ID로 단일 프로젝트 조회
     */
    public Project findById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다: " + projectId));
    }

    /**
     * 회원의 프로젝트 개수 조회
     */
    public int getProjectCountByMember(Long memberId) {
        return findByMemberId(memberId).size();
    }

    /**
     * 회원이 프로젝트를 가지고 있는지 확인
     */
    public boolean hasProjects(Long memberId) {
        return !findByMemberId(memberId).isEmpty();
    }
}
