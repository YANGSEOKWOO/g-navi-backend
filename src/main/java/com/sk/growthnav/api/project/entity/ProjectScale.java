package com.sk.growthnav.api.project.entity;

import lombok.Getter;

@Getter
public enum ProjectScale {
    SMALL("소"),
    MEDIUM("중"),
    MEDIUM_SMALL("중소"),
    LARGE("대"),
    VERY_LARGE("초대"),
    UNKNOWN("미기입");

    private final String label;

    ProjectScale(String label) {
        this.label = label;
    }

}