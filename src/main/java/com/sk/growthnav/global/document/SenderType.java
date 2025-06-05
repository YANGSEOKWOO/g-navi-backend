package com.sk.growthnav.global.document;

import lombok.Getter;

@Getter
public enum SenderType {
    USER("사용자"),
    BOT("봇");

    private final String description;

    SenderType(String description) {
        this.description = description;
    }
}
