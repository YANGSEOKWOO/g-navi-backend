// 1. ExpertiseArea Enum (간소화된 버전)
package com.sk.growthnav.api.member.entity;

import lombok.Getter;

@Getter
public enum ExpertiseArea {
    // 기술 분야
    MANUFACTURE("제조"),
    AI("AI"),
    FINANCE("금융"),
    SEMICONDUCTOR("반도체");

    private final String description;

    ExpertiseArea(String description) {
        this.description = description;
    }
}