package com.sk.growthnav.global.init;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.entity.MemberRole;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.api.news.entity.News;
import com.sk.growthnav.api.news.entity.NewsStatus;
import com.sk.growthnav.api.news.repository.NewsRepository;
import com.sk.growthnav.api.project.entity.Project;
import com.sk.growthnav.api.project.entity.ProjectScale;
import com.sk.growthnav.api.project.repository.ProjectRepository;
import com.sk.growthnav.api.skill.entity.Skill;
import com.sk.growthnav.api.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MockDataInitializer {

    private final MemberRepository memberRepository;
    private final NewsRepository newsRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public void initMockData(String mode) {
        log.info("📦 목업 데이터 초기화 시작 - 모드: {}", mode);

        switch (mode) {
            case "minimal":
                createMinimalData();
                break;
            case "full":
                createFullMockData();
                break;
            default:
                log.warn("알 수 없는 데이터 초기화 모드: {}", mode);
        }
    }

    /**
     * 최소한의 데이터 (운영 환경용)
     */
    private void createMinimalData() {
        log.info("📋 최소한의 목업 데이터 생성...");

        // 기본 EXPERT 1명만 생성
        if (!memberRepository.existsByRole(MemberRole.EXPERT)) {
            Member expert = Member.builder()
                    .name("기본 작성자")
                    .email("expert@gnavi.com")
                    .password("expert123")
                    .role(MemberRole.EXPERT)
                    .isExpert(true)
                    .build();
            memberRepository.save(expert);
            log.info("✅ 기본 Expert 계정 생성: {}", expert.getEmail());
        }
    }

    /**
     * 풍부한 목업 데이터 (로컬 개발 환경용)
     */
    private void createFullMockData() {
        log.info("🎨 전체 목업 데이터 생성...");

        // 1. 목업 사용자들 생성
        List<Member> mockMembers = createMockMembers();

        // 2. 목업 프로젝트들 생성
        List<Project> mockProjects = createMockProjects(mockMembers);

        // 3. 목업 스킬들 생성
        createMockSkills(mockProjects);

        // 4. 목업 뉴스들 생성
        createMockNews(mockMembers);

        log.info("🎉 전체 목업 데이터 생성 완료!");
    }

    private List<Member> createMockMembers() {
        if (memberRepository.count() > 1) {  // Admin 제외하고 이미 데이터가 있으면 스킵
            log.info("⏭️  사용자 데이터가 이미 존재하여 건너뜀");
            return memberRepository.findAll();
        }

        List<Member> members = List.of(
                Member.builder().name("김효준").email("expert1@test.com").password("test123")
                        .role(MemberRole.EXPERT).isExpert(true).level(MemberLevel.CL4).build(),
                Member.builder().name("양석우").email("expert2@test.com").password("test123")
                        .role(MemberRole.EXPERT).isExpert(true).level(MemberLevel.CL5).build(),
                Member.builder().name("이민수").email("user1@test.com").password("test123")
                        .role(MemberRole.USER).isExpert(false).level(MemberLevel.CL2).build(),
                Member.builder().name("박지영").email("user2@test.com").password("test123")
                        .role(MemberRole.USER).isExpert(false).level(MemberLevel.CL3).build(),
                Member.builder().name("최현우").email("user3@test.com").password("test123")
                        .role(MemberRole.USER).isExpert(false).level(MemberLevel.CL1).build()
        );

        List<Member> savedMembers = memberRepository.saveAll(members);
        log.info("👥 목업 사용자 {}명 생성 완료 (등급 포함)", savedMembers.size());
        return savedMembers;
    }

    private List<Project> createMockProjects(List<Member> members) {
        if (projectRepository.count() > 0) {
            log.info("⏭️  프로젝트 데이터가 이미 존재하여 건너뜀");
            return projectRepository.findAll();
        }

        Member user1 = members.stream().filter(m -> m.getEmail().equals("user1@test.com")).findFirst().orElse(members.get(0));
        Member user2 = members.stream().filter(m -> m.getEmail().equals("user2@test.com")).findFirst().orElse(members.get(0));

        List<Project> projects = List.of(
                Project.builder()
                        .name("AI 기반 추천 시스템 개발")
                        .userRole("백엔드 개발자")
                        .domain("AI/ML")
                        .projectScale(ProjectScale.LARGE)
                        .startDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                        .endDate(LocalDateTime.of(2024, 12, 31, 0, 0))
                        .member(user1)
                        .build(),
                Project.builder()
                        .name("모바일 앱 리뉴얼")
                        .userRole("풀스택 개발자")
                        .domain("모바일")
                        .projectScale(ProjectScale.MEDIUM)
                        .startDate(LocalDateTime.of(2024, 6, 1, 0, 0))
                        .endDate(null)  // 진행중
                        .member(user2)
                        .build(),
                Project.builder()
                        .name("데이터 파이프라인 구축")
                        .userRole("데이터 엔지니어")
                        .domain("빅데이터")
                        .projectScale(ProjectScale.MEDIUM)
                        .startDate(LocalDateTime.of(2024, 3, 1, 0, 0))
                        .endDate(LocalDateTime.of(2024, 8, 31, 0, 0))
                        .member(user1)
                        .build()
        );

        List<Project> savedProjects = projectRepository.saveAll(projects);
        log.info("📂 목업 프로젝트 {}개 생성 완료", savedProjects.size());
        return savedProjects;
    }

    private void createMockSkills(List<Project> projects) {
        if (skillRepository.count() > 0) {
            log.info("⏭️  스킬 데이터가 이미 존재하여 건너뜀");
            return;
        }

        List<Skill> skills = List.of(
                // AI 프로젝트 스킬들
                Skill.builder().name("Python").project(projects.get(0)).build(),
                Skill.builder().name("TensorFlow").project(projects.get(0)).build(),
                Skill.builder().name("PostgreSQL").project(projects.get(0)).build(),

                // 모바일 앱 스킬들
                Skill.builder().name("React Native").project(projects.get(1)).build(),
                Skill.builder().name("Node.js").project(projects.get(1)).build(),
                Skill.builder().name("MongoDB").project(projects.get(1)).build(),

                // 데이터 파이프라인 스킬들
                Skill.builder().name("Apache Spark").project(projects.get(2)).build(),
                Skill.builder().name("Kafka").project(projects.get(2)).build(),
                Skill.builder().name("Docker").project(projects.get(2)).build()
        );

        skillRepository.saveAll(skills);
        log.info("🛠️  목업 스킬 {}개 생성 완료", skills.size());
    }

    private void createMockNews(List<Member> members) {
        if (newsRepository.count() > 0) {
            log.info("⏭️  뉴스 데이터가 이미 존재하여 건너뜀");
            return;
        }

        List<Member> experts = members.stream()
                .filter(m -> m.getRole() == MemberRole.EXPERT)
                .toList();

        if (experts.isEmpty()) {
            log.warn("Expert가 없어서 목업 뉴스를 생성할 수 없습니다.");
            return;
        }

        List<News> mockNews = List.of(
                News.builder()
                        .title("메타의 AI 앱 프라이버시 악몽, 사적인 대화 노출")
                        .url("https://news.naver.com/main/read.naver?mode=LSD&mid=sec&sid1=105&oid=001&aid=0014123456")
                        .status(NewsStatus.APPROVED)
                        .expert(experts.get(0))
                        .build(),
                News.builder()
                        .title("기업의 AI 노력이 실패하는 11가지 흔한 이유")
                        .url("https://news.naver.com/main/read.naver?mode=LSD&mid=sec&sid1=105&oid=002&aid=0014789012")
                        .status(NewsStatus.APPROVED)
                        .expert(experts.size() > 1 ? experts.get(1) : experts.get(0))
                        .build(),
                News.builder()
                        .title("2025년 개발자가 주목해야 할 기술 트렌드")
                        .url("https://news.naver.com/main/read.naver?mode=LSD&mid=sec&sid1=105&oid=003&aid=0014345678")
                        .status(NewsStatus.PENDING)
                        .expert(experts.get(0))
                        .build()
        );

        newsRepository.saveAll(mockNews);
        log.info("📰 목업 뉴스 {}개 생성 완료", mockNews.size());
    }
}