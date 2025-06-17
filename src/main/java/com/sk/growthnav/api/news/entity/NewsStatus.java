package com.sk.growthnav.api.news.entity;

import lombok.Getter;

@Getter
public enum NewsStatus {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨");

    private final String description;

    NewsStatus(String description) {
        this.description = description;
    }
}