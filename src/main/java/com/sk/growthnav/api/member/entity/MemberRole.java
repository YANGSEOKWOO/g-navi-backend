package com.sk.growthnav.api.member.entity;

import lombok.Getter;

@Getter
public enum MemberRole {
    USER("일반 사용자"),
    EXPERT("전문가"),
    ADMIN("관리자");

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }
}
