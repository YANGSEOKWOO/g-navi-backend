package com.sk.growthnav.api.project.service;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.project.dto.ProjectInfoDTO;
import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.project.entity.ProjectScale;
import com.sk.growthnav.api.project.repository.ProjectRepository;
import com.sk.growthnav.api.skill.service.SkillService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 테스트")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SkillService skillService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("회원의 모든 프로젝트 조회 성공")
    void findByMemberId_Success() {
        // Given
        Long memberId = 1L;
        List<Project> mockProjects = createMockProjects(memberId);

        given(projectRepository.findByMemberId(memberId))
                .willReturn(mockProjects);

        // When
        List<Project> result = projectService.findByMemberId(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("스마트팩토리 구축");
        assertThat(result.get(1).getName()).isEqualTo("ERP 시스템 개선");

        then(projectRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("회원에게 프로젝트가 없는 경우 빈 리스트 반환")
    void findByMemberId_EmptyList() {
        // Given
        Long memberId = 999L;
        given(projectRepository.findByMemberId(memberId))
                .willReturn(List.of());

        // When
        List<Project> result = projectService.findByMemberId(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        then(projectRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("회원의 프로젝트 정보를 DTO로 변환하여 조회 성공")
    void getProjectsByMember_Success() {
        // Given
        Long memberId = 1L;
        List<Project> mockProjects = createMockProjects(memberId);

        given(projectRepository.findByMemberId(memberId))
                .willReturn(mockProjects);

        // 각 프로젝트별 스킬 Mock 설정
        given(skillService.getSkillNamesByProject(1L))
                .willReturn(Arrays.asList("Java", "Spring Boot", "PostgreSQL"));
        given(skillService.getSkillNamesByProject(2L))
                .willReturn(Arrays.asList("Python", "Django"));

        // When
        List<ProjectInfoDTO> result = projectService.getProjectsByMember(memberId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // 첫 번째 프로젝트 검증
        ProjectInfoDTO firstProject = result.get(0);
        assertThat(firstProject.getProjectName()).isEqualTo("스마트팩토리 구축");
        assertThat(firstProject.getRole()).isEqualTo("Backend Developer");
        assertThat(firstProject.getDomain()).isEqualTo("제조");
        assertThat(firstProject.getScale()).isEqualTo("대");
        assertThat(firstProject.getStartDate()).isEqualTo("2023-01-15");
        assertThat(firstProject.getEndDate()).isEqualTo("2023-12-31");
        assertThat(firstProject.getSkills()).containsExactly("Java", "Spring Boot", "PostgreSQL");

        // 두 번째 프로젝트 검증
        ProjectInfoDTO secondProject = result.get(1);
        assertThat(secondProject.getProjectName()).isEqualTo("ERP 시스템 개선");
        assertThat(secondProject.getRole()).isEqualTo("Application PM");
        assertThat(secondProject.getDomain()).isEqualTo("금융");
        assertThat(secondProject.getScale()).isEqualTo("중");
        assertThat(secondProject.getStartDate()).isEqualTo("2024-03-01");
        assertThat(secondProject.getEndDate()).isNull(); // 진행중인 프로젝트
        assertThat(secondProject.getSkills()).containsExactly("Python", "Django");

        then(projectRepository).should(times(1)).findByMemberId(memberId);
        then(skillService).should(times(1)).getSkillNamesByProject(1L);
        then(skillService).should(times(1)).getSkillNamesByProject(2L);
    }

    @Test
    @DisplayName("프로젝트 ID로 단일 프로젝트 조회 성공")
    void findById_Success() {
        // Given
        Long projectId = 1L;
        Project mockProject = createMockProject(1L, "스마트팩토리 구축", 1L);

        given(projectRepository.findById(projectId))
                .willReturn(Optional.of(mockProject));

        // When
        Project result = projectService.findById(projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(projectId);
        assertThat(result.getName()).isEqualTo("스마트팩토리 구축");

        then(projectRepository).should(times(1)).findById(projectId);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 조회 시 예외 발생")
    void findById_ProjectNotFound() {
        // Given
        Long nonExistentId = 999L;
        given(projectRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.findById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없습니다: " + nonExistentId);

        then(projectRepository).should(times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("회원의 프로젝트 개수 조회 성공")
    void getProjectCountByMember_Success() {
        // Given
        Long memberId = 1L;
        List<Project> mockProjects = createMockProjects(memberId);

        given(projectRepository.findByMemberId(memberId))
                .willReturn(mockProjects);

        // When
        int result = projectService.getProjectCountByMember(memberId);

        // Then
        assertThat(result).isEqualTo(2);
        then(projectRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("회원이 프로젝트를 가지고 있는지 확인 - 있는 경우")
    void hasProjects_True() {
        // Given
        Long memberId = 1L;
        List<Project> mockProjects = createMockProjects(memberId);

        given(projectRepository.findByMemberId(memberId))
                .willReturn(mockProjects);

        // When
        boolean result = projectService.hasProjects(memberId);

        // Then
        assertThat(result).isTrue();
        then(projectRepository).should(times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("회원이 프로젝트를 가지고 있는지 확인 - 없는 경우")
    void hasProjects_False() {
        // Given
        Long memberId = 999L;
        given(projectRepository.findByMemberId(memberId))
                .willReturn(List.of());

        // When
        boolean result = projectService.hasProjects(memberId);

        // Then
        assertThat(result).isFalse();
        then(projectRepository).should(times(1)).findByMemberId(memberId);
    }

    // === Helper Methods ===

    private List<Project> createMockProjects(Long memberId) {
        Member mockMember = new Member(memberId, "오현진", "password", "test@example.com");

        return Arrays.asList(
                new Project(
                        1L,
                        "스마트팩토리 구축",
                        "Backend Developer",
                        "제조",
                        ProjectScale.LARGE,
                        LocalDateTime.of(2023, 1, 15, 0, 0),
                        LocalDateTime.of(2023, 12, 31, 0, 0),
                        mockMember
                ),
                new Project(
                        2L,
                        "ERP 시스템 개선",
                        "Application PM",
                        "금융",
                        ProjectScale.MEDIUM,
                        LocalDateTime.of(2024, 3, 1, 0, 0),
                        null, // 진행중인 프로젝트
                        mockMember
                )
        );
    }

    private Project createMockProject(Long id, String name, Long memberId) {
        Member mockMember = new Member(memberId, "테스트 사용자", "password", "test@example.com");

        return new Project(
                id,
                name,
                "Backend Developer",
                "IT",
                ProjectScale.LARGE,
                LocalDateTime.of(2023, 1, 15, 0, 0),
                LocalDateTime.of(2023, 12, 31, 0, 0),
                mockMember
        );
    }
}