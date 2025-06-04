package com.sk.growthnav.api.message.entity;

import lombok.Getter;

@Getter
public enum SenderType {
    USER("사용자"),
    BOT("챗봇");


    private final String label;

    SenderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}