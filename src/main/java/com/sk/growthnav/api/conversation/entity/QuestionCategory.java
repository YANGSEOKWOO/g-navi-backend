package com.sk.growthnav.api.conversation.entity;

import lombok.Getter;

@Getter
public enum QuestionCategory {
    CAREER("커리어질문"),
    SKILL("스킬질문"),
    PROJECT("프로젝트질문"),
    OTHER("기타");

    private final String description;

    QuestionCategory(String description) {
        this.description = description;
    }

    /**
     * 메시지 내용을 분석해서 카테고리 추정
     * 실제로는 AI나 더 정교한 로직으로 분류할 수 있음
     */
    public static QuestionCategory categorizeMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return OTHER;
        }

        String lowerMessage = messageText.toLowerCase();

        // 커리어 관련 키워드
        if (containsAny(lowerMessage, "커리어", "직무", "취업", "이직", "진로", "경력", "승진", "연봉", "면접", "포트폴리오")) {
            return CAREER;
        }

        // 스킬 관련 키워드
        if (containsAny(lowerMessage, "스킬", "기술", "언어", "프레임워크", "라이브러리", "java", "python", "javascript", "react", "spring", "학습", "공부")) {
            return SKILL;
        }

        // 프로젝트 관련 키워드
        if (containsAny(lowerMessage, "프로젝트", "개발", "구현", "설계", "아키텍처", "시스템", "서비스", "애플리케이션", "웹", "앱")) {
            return PROJECT;
        }

        return OTHER;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}