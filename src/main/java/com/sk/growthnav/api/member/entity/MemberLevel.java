package com.sk.growthnav.api.member.entity;

import lombok.Getter;

@Getter
public enum MemberLevel {
    CL1("CL1"),
    CL2("CL2"),
    CL3("CL3"),
    CL4("CL4"),
    CL5("CL5");

    private final String label;

    MemberLevel(String label) {
        this.label = label;
    }
}