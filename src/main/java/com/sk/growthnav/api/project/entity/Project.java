package com.sk.growthnav.api.project.entity;


import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String userRole;

    @Column(length = 30, nullable = false)
    private String domain;

    @Enumerated(EnumType.STRING)
    private ProjectScale projectScale;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Project create(String name, String userRole, String domain,
                                 ProjectScale projectScale, LocalDateTime startDate,
                                 LocalDateTime endDate, Member member) {
        return Project.builder()
                .name(name)
                .userRole(userRole)
                .domain(domain)
                .projectScale(projectScale)
                .startDate(startDate)
                .endDate(endDate)
                .member(member)
                .build();
    }

}
