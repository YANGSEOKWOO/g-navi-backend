package com.sk.growthnav.api.skill.service;

import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.project.entity.ProjectScale;
import com.sk.growthnav.api.skill.dto.SkillInfoDTO;
import com.sk.growthnav.api.skill.entity.Skill;
import com.sk.growthnav.api.skill.repository.SkillRepository;
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
@DisplayName("SkillService 테스트")
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    @DisplayName("프로젝트의 모든 스킬 이름 조회 성공")
    void getSkillNamesByProject_Success() {
        // Given
        Long projectId = 1L;
        Project mockProject = createMockProject(projectId, "테스트 프로젝트");

        List<Skill> mockSkills = Arrays.asList(
                createMockSkill(1L, "Java", mockProject),
                createMockSkill(2L, "Spring Boot", mockProject),
                createMockSkill(3L, "PostgreSQL", mockProject)
        );

        given(skillRepository.findAllByProjectId(projectId))
                .willReturn(mockSkills);

        // When
        List<String> result = skillService.getSkillNamesByProject(projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Java", "Spring Boot", "PostgreSQL");

        then(skillRepository).should(times(1)).findAllByProjectId(projectId);
    }

    @Test
    @DisplayName("프로젝트에 스킬이 없는 경우 빈 리스트 반환")
    void getSkillNamesByProject_EmptyList() {
        // Given
        Long projectId = 999L;
        given(skillRepository.findAllByProjectId(projectId))
                .willReturn(List.of());

        // When
        List<String> result = skillService.getSkillNamesByProject(projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        then(skillRepository).should(times(1)).findAllByProjectId(projectId);
    }

    @Test
    @DisplayName("프로젝트의 모든 스킬 DTO 조회 성공")
    void getSkillsByProject_Success() {
        // Given
        Long projectId = 1L;
        Project mockProject = createMockProject(projectId, "테스트 프로젝트");

        List<Skill> mockSkills = Arrays.asList(
                createMockSkill(1L, "Java", mockProject),
                createMockSkill(2L, "Spring Boot", mockProject)
        );

        given(skillRepository.findAllByProjectId(projectId))
                .willReturn(mockSkills);

        // When
        List<SkillInfoDTO> result = skillService.getSkillsByProject(projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSkillName()).isEqualTo("Java");
        assertThat(result.get(0).getProjectId()).isEqualTo(projectId);
        assertThat(result.get(1).getSkillName()).isEqualTo("Spring Boot");

        then(skillRepository).should(times(1)).findAllByProjectId(projectId);
    }

    @Test
    @DisplayName("스킬 ID로 단일 스킬 조회 성공")
    void findById_Success() {
        // Given
        Long skillId = 1L;
        Project mockProject = createMockProject(1L, "테스트 프로젝트");
        Skill mockSkill = createMockSkill(skillId, "Java", mockProject);

        given(skillRepository.findById(skillId))
                .willReturn(Optional.of(mockSkill));

        // When
        Skill result = skillService.findById(skillId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(skillId);
        assertThat(result.getName()).isEqualTo("Java");

        then(skillRepository).should(times(1)).findById(skillId);
    }

    @Test
    @DisplayName("존재하지 않는 스킬 ID로 조회 시 예외 발생")
    void findById_SkillNotFound() {
        // Given
        Long nonExistentId = 999L;
        given(skillRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> skillService.findById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("스킬을 찾을 수 없습니다: " + nonExistentId);

        then(skillRepository).should(times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("프로젝트에 스킬이 있는지 확인 - 있는 경우")
    void hasSkills_True() {
        // Given
        Long projectId = 1L;
        given(skillRepository.existsByProjectId(projectId))
                .willReturn(true);

        // When
        boolean result = skillService.hasSkills(projectId);

        // Then
        assertThat(result).isTrue();
        then(skillRepository).should(times(1)).existsByProjectId(projectId);
    }

    @Test
    @DisplayName("프로젝트에 스킬이 있는지 확인 - 없는 경우")
    void hasSkills_False() {
        // Given
        Long projectId = 999L;
        given(skillRepository.existsByProjectId(projectId))
                .willReturn(false);

        // When
        boolean result = skillService.hasSkills(projectId);

        // Then
        assertThat(result).isFalse();
        then(skillRepository).should(times(1)).existsByProjectId(projectId);
    }

    // === Helper Methods ===

    private Project createMockProject(Long id, String name) {
        return new Project(
                id,
                name,
                "Backend Developer",
                "IT",
                ProjectScale.LARGE,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(6),
                null  // Member는 null로 설정
        );
    }

    private Skill createMockSkill(Long id, String name, Project project) {
        return Skill.builder()
                .id(id)
                .name(name)
                .project(project)
                .build();
    }
}