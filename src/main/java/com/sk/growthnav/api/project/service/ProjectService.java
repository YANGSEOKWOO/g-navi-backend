package com.sk.growthnav.api.project.service;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.service.MemberService;
import com.sk.growthnav.api.project.dto.ProjectCreateRequest;
import com.sk.growthnav.api.project.dto.ProjectCreateResponse;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.project.repository.ProjectRepository;
import com.sk.growthnav.api.skill.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SkillService skillService;
    private final MemberService memberService;

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

    /**
     * 프로젝트 생성
     */
    @Transactional
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        log.info("프로젝트 생성 시작, memberId={}, projectName={}", request.getMemberId(), request.getProjectName());

        // 1. 회원 존재 여부 확인
        Member member = memberService.findById(request.getMemberId());

        // 2. 프로젝트 생성
        Project project = createProjectEntity(request, member);
        Project savedProject = projectRepository.save(project);
        log.info("프로젝트 저장 완료: projectId={}", savedProject.getId());

        // 3. 스킬 생성 (Project가 저장된 후 스킬들을 저장)
        List<String> skillNames = skillService.createSkills(savedProject, request.getSkills());
        log.info("스킬 저장 완료: projectId={}, skillCount={}", savedProject.getId(), skillNames.size());

        // 4. 응답생성
        ProjectCreateResponse response = ProjectCreateResponse.of(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getUserRole(),
                savedProject.getDomain(),
                savedProject.getProjectScale().getLabel(),
                savedProject.getStartDate().toLocalDate(),
                savedProject.getEndDate() != null ? savedProject.getEndDate().toLocalDate() : null,
                skillNames
        );

        log.info("프로젝트 생성 완료: projectId={}", savedProject.getId());
        return response;

    }

    private Project createProjectEntity(ProjectCreateRequest request, Member member) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate() != null
                ? request.getEndDate().atTime(23, 59, 59)
                : null;
        return Project.builder()
                .name(request.getProjectName())
                .userRole(request.getUserRole())
                .domain(request.getDomain())
                .projectScale(request.getProjectScale())
                .startDate(startDateTime)
                .endDate(endDateTime)
                .member(member)
                .build();
    }

    /**
     * 프로젝트 상세 조회 (Controller에서 호출)
     */
    public ProjectInfoDTO getProjectDetail(Long projectId) {
        log.info("프로젝트 상세 조회: projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다: " + projectId));

        List<String> skills = skillService.getSkillNamesByProject(projectId);
        return ProjectInfoDTO.from(project, skills);
    }

    /**
     * 프로젝트 삭제 (Controller에서 호출)
     */
    @Transactional
    public void deleteProject(Long projectId) {
        log.info("프로젝트 삭제: projectId={}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다: " + projectId));

        // 스킬들도 함께 삭제 (CASCADE 설정이 되어있다면 자동, 아니면 수동 삭제)
        skillService.deleteSkillsByProject(projectId);
        projectRepository.delete(project);

        log.info("프로젝트 삭제 완료: projectId={}", projectId);
    }
}
