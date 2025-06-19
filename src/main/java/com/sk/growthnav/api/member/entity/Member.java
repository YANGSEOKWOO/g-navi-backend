package com.sk.growthnav.api.member.entity;

import com.sk.growthnav.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", unique = true, nullable = false)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberRole role = MemberRole.USER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isExpert = false;

    // 추가된 등급 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberLevel level = MemberLevel.CL1;  // 기본값: CL1

    // 역할 변경 메서드
    public void changeRole(MemberRole newRole) {
        this.role = newRole;
        this.isExpert = (newRole == MemberRole.EXPERT);
    }

    // 등급 변경 메서드
    public void changeLevel(MemberLevel newLevel) {
        this.level = newLevel;
    }

    // 전문가 여부 확인
    public boolean isExpert() {
        return this.isExpert;
    }

    // 관리자 여부 확인
    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }

    public boolean isEXPERT() {
        return this.role == MemberRole.EXPERT || this.role == MemberRole.ADMIN;
    }
}