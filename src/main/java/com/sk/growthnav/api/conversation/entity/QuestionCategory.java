package com.sk.growthnav.api.conversation.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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
     * 고도화된 메시지 카테고리 분류
     * 1. 키워드 가중치 기반 점수 계산
     * 2. 문맥 패턴 분석
     * 3. 부정어 처리
     * 4. 복합 키워드 인식
     */
    public static QuestionCategory categorizeMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return OTHER;
        }

        String normalizedText = normalizeText(messageText);

        // 1단계: 가중치 기반 점수 계산
        Map<QuestionCategory, Double> categoryScores = calculateCategoryScores(normalizedText);

        // 2단계: 문맥 패턴 보너스 적용
        applyContextPatternBonus(normalizedText, categoryScores);

        // 3단계: 부정어 패널티 적용
        applyNegationPenalty(normalizedText, categoryScores);

        // 4단계: 최고 점수 카테고리 선택 (임계값 적용)
        return selectBestCategory(categoryScores);
    }

    /**
     * 텍스트 정규화
     */
    private static String normalizeText(String text) {
        return text.toLowerCase()
                .replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")  // 특수문자 제거
                .replaceAll("\\s+", " ")                   // 연속 공백 정리
                .trim();
    }

    /**
     * 카테고리별 가중치 기반 점수 계산
     */
    private static Map<QuestionCategory, Double> calculateCategoryScores(String text) {
        Map<QuestionCategory, Double> scores = new HashMap<>();

        // 각 카테고리별로 초기 점수 0으로 설정
        for (QuestionCategory category : values()) {
            scores.put(category, 0.0);
        }

        // CAREER 점수 계산
        scores.put(CAREER, calculateCareerScore(text));

        // SKILL 점수 계산
        scores.put(SKILL, calculateSkillScore(text));

        // PROJECT 점수 계산
        scores.put(PROJECT, calculateProjectScore(text));

        return scores;
    }

    /**
     * 커리어 관련 점수 계산
     */
    private static double calculateCareerScore(String text) {
        double score = 0.0;

        // 고가중치 키워드 (3점)
        String[] highWeightCareer = {
                "커리어", "진로", "직업", "직무", "취업", "이직", "전직", "경력", "승진", "연봉", "급여"
        };
        score += countKeywords(text, highWeightCareer) * 3.0;

        // 중가중치 키워드 (2점)
        String[] mediumWeightCareer = {
                "면접", "이력서", "포트폴리오", "자기소개", "회사", "기업", "채용", "입사", "퇴사"
        };
        score += countKeywords(text, mediumWeightCareer) * 2.0;

        // 저가중치 키워드 (1점)
        String[] lowWeightCareer = {
                "회사생활", "업무", "상사", "동료", "팀", "부서", "평가", "성과"
        };
        score += countKeywords(text, lowWeightCareer) * 1.0;

        return score;
    }

    /**
     * 스킬 관련 점수 계산
     */
    private static double calculateSkillScore(String text) {
        double score = 0.0;

        // 고가중치: 명시적 기술 용어 (3점)
        String[] techKeywords = {
                "java", "python", "javascript", "react", "spring", "node", "vue", "angular",
                "mysql", "postgresql", "mongodb", "redis", "docker", "kubernetes", "aws",
                "git", "linux", "typescript", "html", "css", "android", "ios", "swift"
        };
        score += countKeywords(text, techKeywords) * 3.0;

        // 중가중치: 스킬 관련 일반 용어 (2점)
        String[] skillKeywords = {
                "스킬", "기술", "언어", "프레임워크", "라이브러리", "도구", "툴",
                "프로그래밍", "코딩", "개발언어", "기술스택"
        };
        score += countKeywords(text, skillKeywords) * 2.0;

        // 저가중치: 학습 관련 (1점)
        String[] learningKeywords = {
                "학습", "공부", "배우", "익히", "연습", "실습", "강의", "튜토리얼", "책"
        };
        score += countKeywords(text, learningKeywords) * 1.0;

        return score;
    }

    /**
     * 프로젝트 관련 점수 계산
     */
    private static double calculateProjectScore(String text) {
        double score = 0.0;

        // 고가중치: 프로젝트 핵심 용어 (3점)
        String[] projectKeywords = {
                "프로젝트", "개발", "구현", "제작", "구축", "시스템", "서비스", "애플리케이션"
        };
        score += countKeywords(text, projectKeywords) * 3.0;

        // 중가중치: 개발 프로세스 (2점)
        String[] processKeywords = {
                "설계", "아키텍처", "요구사항", "분석", "테스트", "배포", "운영"
        };
        score += countKeywords(text, processKeywords) * 2.0;

        // 저가중치: 개발 도메인 (1점)
        String[] domainKeywords = {
                "웹", "앱", "모바일", "백엔드", "프론트엔드", "api", "데이터베이스"
        };
        score += countKeywords(text, domainKeywords) * 1.0;

        return score;
    }

    /**
     * 키워드 개수 카운트
     */
    private static int countKeywords(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 문맥 패턴 보너스 적용
     */
    private static void applyContextPatternBonus(String text, Map<QuestionCategory, Double> scores) {
        // 질문 패턴 인식
        if (containsQuestionPattern(text)) {
            // 질문 형태일 때 모든 점수에 1.2배 가중치
            scores.replaceAll((category, score) -> score * 1.2);
        }

        // CAREER 특화 패턴
        if (containsCareerPattern(text)) {
            scores.put(CAREER, scores.get(CAREER) + 2.0);
        }

        // SKILL 특화 패턴
        if (containsSkillPattern(text)) {
            scores.put(SKILL, scores.get(SKILL) + 2.0);
        }

        // PROJECT 특화 패턴
        if (containsProjectPattern(text)) {
            scores.put(PROJECT, scores.get(PROJECT) + 2.0);
        }
    }

    /**
     * 질문 패턴 감지
     */
    private static boolean containsQuestionPattern(String text) {
        String[] questionPatterns = {
                "어떻게", "어떤", "무엇", "왜", "언제", "어디서", "어느", "얼마나",
                "방법", "추천", "조언", "도움", "궁금", "알고싶", "배우고싶"
        };
        return containsAny(text, questionPatterns);
    }

    /**
     * 커리어 특화 패턴
     */
    private static boolean containsCareerPattern(String text) {
        return text.matches(".*\\b(\\d+)년차\\b.*") ||  // "3년차" 패턴
                text.contains("어떤 회사") ||
                text.contains("어떤 직무") ||
                text.contains("이직하려면") ||
                text.contains("취업하려면");
    }

    /**
     * 스킬 특화 패턴
     */
    private static boolean containsSkillPattern(String text) {
        return text.matches(".*\\b\\w+(을|를)\\s*(배우|학습|공부).*") ||  // "자바를 배우고" 패턴
                text.contains("기술을") ||
                text.contains("어떤 언어") ||
                text.contains("어떤 기술");
    }

    /**
     * 프로젝트 특화 패턴
     */
    private static boolean containsProjectPattern(String text) {
        return text.contains("프로젝트를") ||
                text.contains("개발하려면") ||
                text.contains("구현하려면") ||
                text.matches(".*\\b\\w+\\s*(프로젝트|시스템|서비스).*");
    }

    /**
     * 부정어 패널티 적용
     */
    private static void applyNegationPenalty(String text, Map<QuestionCategory, Double> scores) {
        String[] negationWords = {"안", "못", "아니", "없", "말고", "빼고", "제외"};

        for (String negation : negationWords) {
            if (text.contains(negation)) {
                // 부정어가 있으면 모든 점수를 0.8배로 감소
                scores.replaceAll((category, score) -> score * 0.8);
                break;
            }
        }
    }

    /**
     * 최적 카테고리 선택
     */
    private static QuestionCategory selectBestCategory(Map<QuestionCategory, Double> scores) {
        // OTHER 제외하고 최고 점수 찾기
        double maxScore = 0.0;
        QuestionCategory bestCategory = OTHER;

        for (Map.Entry<QuestionCategory, Double> entry : scores.entrySet()) {
            if (entry.getKey() != OTHER && entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        // 임계값 확인 (최소 2점 이상이어야 분류)
        if (maxScore >= 2.0) {
            return bestCategory;
        }

        return OTHER;
    }

    /**
     * 키워드 포함 여부 확인 (기존 메서드 유지)
     */
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}