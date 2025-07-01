package com.sk.growthnav.api.wordcloud.service;

import com.sk.growthnav.api.conversation.document.ConversationDocument;
import com.sk.growthnav.api.conversation.entity.QuestionCategory;
import com.sk.growthnav.api.conversation.repository.ConversationRepository;
import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.api.member.entity.MemberLevel;
import com.sk.growthnav.api.member.repository.MemberRepository;
import com.sk.growthnav.api.wordcloud.dto.WordCloudResponse;
import com.sk.growthnav.global.document.SenderType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordCloudService {

    private final ConversationRepository conversationRepository;
    private final MemberRepository memberRepository;

    // ✅ 대폭 확장된 한국어 불용어 목록
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            // 조사, 어미, 접사
            "이", "가", "을", "를", "에", "에서", "와", "과", "의", "로", "으로", "부터", "까지", "에게", "한테",
            "은", "는", "도", "만", "라도", "이나", "나", "든지", "이든지", "라든지", "이라도", "이면", "면",
            "하고", "하며", "이며", "며", "고", "지만", "습니다", "입니다", "있습니다", "됩니다", "해요", "이에요",

            // 대명사, 지시어
            "이것", "그것", "저것", "여기", "거기", "저기", "이거", "그거", "저거", "이런", "그런", "저런",
            "이렇게", "그렇게", "저렇게", "어디", "언제", "무엇", "누구", "왜", "어떻게", "얼마나", "얼마",
            "이제", "지금", "그때", "언제나", "항상", "가끔", "때때로", "자주",

            // 일반적인 단어
            "있다", "없다", "이다", "되다", "하다", "같다", "다르다", "좋다", "나쁘다", "크다", "작다",
            "많다", "적다", "높다", "낮다", "빠르다", "느리다", "새롭다", "오래되다", "쉽다", "어렵다",

            // 시간 관련
            "오늘", "어제", "내일", "지금", "현재", "나중에", "먼저", "다음", "이전", "요즘", "최근", "옛날",
            "아침", "점심", "저녁", "밤", "새벽", "오후", "오전",

            // 기타 일반적인 단어
            "정말", "진짜", "아주", "매우", "너무", "조금", "많이", "잘", "못", "안", "또", "그리고",
            "하지만", "그러나", "그래서", "따라서", "때문에", "위해", "위한", "통해", "대해", "관해",
            "사람", "것", "일", "때", "곳", "점", "면", "경우", "상황", "문제", "방법", "결과", "시간",

            // 질문/대화 관련 불용어 (🔥 여기가 핵심!)
            "질문", "궁금", "알고", "싶습니다", "싶어요", "해주세요", "알려주세요", "도움", "부탁", "문의",
            "요청", "설명", "답변", "추천", "의견", "생각", "조언", "어떤", "가진", "나와", "했어", "알려줘",
            "추천해줘", "작성해줘", "좋을까", "있을까", "하면", "다시", "나는", "알아", "하고싶어", "있을까요",
            "뭐야", "안녕", "안녕하세요", "헬로", "굿모닝", "고마워", "뭐해", "좀쳐", "그냥", "좋아", "아니",
            "할래", "뭐해", "다른", "말고", "있어", "되고싶어", "관련", "그냥",

            // 단위, 수량, 접속사
            "개", "명", "번", "차", "년", "월", "시", "분", "초", "원", "달러", "퍼센트",
            "그럼", "그러면", "만약", "혹시", "아마", "아마도", "확실히", "분명히", "당연히", "물론",

            // 반복되는 의미없는 표현들 (실제 데이터에서 추출)
            "구성원들은", "유사한", "경로로", "성장", "선배", "경력을", "직무로", "있는지", "스택으로", "나아갈",
            "앞으로", "기반으로", "경험을", "프로젝트를", "쌓아야", "커리어를", "있을까", "성장하고", "줘봐",
            "개발을", "개발자로", "성장하기", "고민이에요", "단계별로", "준비하면", "비슷한", "어느정도",
            "위해서는", "계획", "형식으로", "짜줘", "떼고", "zz", "렉이너무심해요", "옛날거잖아", "pdp",
            "이모지", "오직", "풀스택", "옛날거잔아", "성장했어", "비슷한", "데이터분석을", "컴퓨팅",
            "전공하고", "기술스택이", "직업", "성장했는지", "했다", "형식으로", "위해서는", "참여를",
            "참여하려고", "애니메이션을", "백엔드개발관련", "원칙에", "장인이", "추천해준거", "생산성을",
            "금융프로젝트", "임베디드"
    ));

    // ✅ 의미있는 기술/도메인 키워드만 추출하기 위한 화이트리스트
    private static final Set<String> MEANINGFUL_KEYWORDS = new HashSet<>(Arrays.asList(
            // 프로그래밍 언어
            "java", "python", "javascript", "typescript", "kotlin", "swift", "go", "rust", "c", "cpp",
            "ruby", "php", "scala", "clojure", "dart", "r", "matlab",

            // 프레임워크/라이브러리
            "spring", "react", "vue", "angular", "nodejs", "express", "fastapi", "django", "flask",
            "bootstrap", "tailwind", "jquery", "axios", "redux", "mobx",

            // 데이터베이스
            "mysql", "postgresql", "mongodb", "redis", "elasticsearch", "oracle", "sqlite", "cassandra",
            "dynamodb", "firebase", "mariadb",

            // 클라우드/인프라
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "github", "gitlab", "terraform",
            "ansible", "nginx", "apache", "tomcat", "클라우드",

            // 개발 도구
            "git", "intellij", "vscode", "eclipse", "postman", "swagger", "jira", "confluence",
            "slack", "notion", "figma",

            // 도메인/분야
            "ai", "머신러닝", "딥러닝", "데이터분석", "빅데이터", "블록체인", "iot", "ar", "vr",
            "fintech", "금융", "핀테크", "이커머스", "게임", "헬스케어", "의료", "교육", "에듀테크",
            "물류", "제조", "제조업", "스마트팩토리", "자동차", "모빌리티",

            // 역할/직무
            "백엔드", "프론트엔드", "풀스택", "데브옵스", "pm", "po", "기획자", "디자이너", "qa", "dba",
            "아키텍트", "테크리드", "cto", "개발팀장",

            // 개발 개념/방법론
            "api", "restapi", "graphql", "microservice", "msa", "tdd", "bdd", "ci", "cd", "devops",
            "agile", "scrum", "kanban", "객체지향", "함수형", "reactive", "asynchronous", "동기", "비동기",

            // 기술 스택
            "기술스택", "스택", "아키텍처", "인프라", "보안", "성능", "최적화", "리팩토링", "테스팅",
            "모니터링", "로깅", "배포", "운영", "기술",

            // 커리어/성장
            "커리어", "이직", "취업", "면접", "포트폴리오", "경력", "연차", "주니어", "시니어", "리드",
            "매니저", "팀장", "승진", "연봉", "급여", "회사", "스타트업", "대기업", "중소기업",

            // 프로젝트
            "프로젝트", "개발", "구현", "설계", "요구사항", "기획", "디자인", "테스트", "배포", "런칭",
            "서비스", "플랫폼", "시스템", "애플리케이션", "웹", "앱", "모바일",

            // 학습/성장
            "학습", "공부", "스터디", "교육", "강의", "책", "문서", "튜토리얼", "실습", "연습",
            "자격증", "인증", "부트캠프", "코딩테스트", "알고리즘", "자료구조"
    ));

    // ✅ 정규식 패턴들
    private static final Pattern ENGLISH_ONLY = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern KOREAN_ONLY = Pattern.compile("^[가-힣]+$");
    private static final Pattern MIXED_VALID = Pattern.compile("^[가-힣a-zA-Z0-9]+$");
    private static final Pattern MEANINGLESS_PATTERN = Pattern.compile("^(ㅋ+|ㅎ+|ㅜ+|ㅠ+|ㅡ+|ㅂ+|ㄷ+|ㄱ+)$");

    @PostConstruct
    public void init() {
        log.info("WordCloudService 초기화 완료");
        log.info("불용어 개수: {}", STOP_WORDS.size());
        log.info("의미있는 키워드 개수: {}", MEANINGFUL_KEYWORDS.size());
    }

    /**
     * 전체 사용자 질문 워드클라우드 생성
     */
    public WordCloudResponse getAllUserQuestionsWordCloud(Integer maxWords) {
        log.info("전체 사용자 질문 워드클라우드 생성 시작: maxWords={}", maxWords);

        try {
            // 모든 대화 조회
            List<ConversationDocument> allConversations = conversationRepository.findAll();

            // 사용자 메시지만 추출
            List<String> userMessages = extractUserMessages(allConversations);

            if (userMessages.isEmpty()) {
                log.warn("분석할 사용자 메시지가 없음");
                return WordCloudResponse.empty();
            }

            // 단어 빈도수 계산
            Map<String, Integer> wordCounts = calculateWordFrequency(userMessages);

            // 상위 N개 단어 선택 및 응답 생성
            List<WordCloudResponse.WordCloudWord> topWords = selectTopWords(wordCounts, maxWords);

            log.info("전체 사용자 질문 워드클라우드 생성 완료: totalMessages={}, uniqueWords={}",
                    userMessages.size(), topWords.size());

            return WordCloudResponse.of(topWords, userMessages.size());

        } catch (Exception e) {
            log.error("전체 사용자 질문 워드클라우드 생성 중 오류: {}", e.getMessage(), e);
            return WordCloudResponse.empty();
        }
    }

    /**
     * 특정 등급 사용자 질문 워드클라우드 생성
     */
    public WordCloudResponse getLevelUserQuestionsWordCloud(String levelStr, Integer maxWords) {
        log.info("등급별 사용자 질문 워드클라우드 생성 시작: level={}, maxWords={}", levelStr, maxWords);

        try {
            MemberLevel level = MemberLevel.valueOf(levelStr.toUpperCase());

            // 해당 등급의 모든 사용자 조회
            List<Member> members = memberRepository.findByLevel(level);
            if (members.isEmpty()) {
                log.warn("등급 {}에 해당하는 사용자가 없음", level);
                return WordCloudResponse.empty();
            }

            List<Long> memberIds = members.stream()
                    .map(Member::getId)
                    .collect(Collectors.toList());

            // 해당 사용자들의 대화 조회
            List<ConversationDocument> conversations = conversationRepository.findByMemberIdIn(memberIds);

            // 사용자 메시지만 추출
            List<String> userMessages = extractUserMessages(conversations);

            if (userMessages.isEmpty()) {
                log.warn("등급 {}의 사용자 메시지가 없음", level);
                return WordCloudResponse.empty();
            }

            // 단어 빈도수 계산
            Map<String, Integer> wordCounts = calculateWordFrequency(userMessages);

            // 상위 N개 단어 선택
            List<WordCloudResponse.WordCloudWord> topWords = selectTopWords(wordCounts, maxWords);

            log.info("등급별 사용자 질문 워드클라우드 생성 완료: level={}, totalMessages={}, uniqueWords={}",
                    level, userMessages.size(), topWords.size());

            return WordCloudResponse.of(topWords, userMessages.size());

        } catch (IllegalArgumentException e) {
            log.error("잘못된 등급 값: levelStr={}", levelStr);
            throw new IllegalArgumentException("유효하지 않은 등급입니다: " + levelStr);
        } catch (Exception e) {
            log.error("등급별 사용자 질문 워드클라우드 생성 중 오류: level={}, error={}", levelStr, e.getMessage(), e);
            return WordCloudResponse.empty();
        }
    }

    /**
     * 특정 사용자 질문 워드클라우드 생성
     */
    public WordCloudResponse getUserQuestionsWordCloud(Long userId, Integer maxWords) {
        log.info("개인 사용자 질문 워드클라우드 생성 시작: userId={}, maxWords={}", userId, maxWords);

        try {
            // 해당 사용자의 모든 대화 조회
            List<ConversationDocument> conversations = conversationRepository.findByMemberId(userId);

            // 사용자 메시지만 추출
            List<String> userMessages = extractUserMessages(conversations);

            if (userMessages.isEmpty()) {
                log.warn("사용자 {}의 메시지가 없음", userId);
                return WordCloudResponse.empty();
            }

            // 단어 빈도수 계산
            Map<String, Integer> wordCounts = calculateWordFrequency(userMessages);

            // 상위 N개 단어 선택
            List<WordCloudResponse.WordCloudWord> topWords = selectTopWords(wordCounts, maxWords);

            log.info("개인 사용자 질문 워드클라우드 생성 완료: userId={}, totalMessages={}, uniqueWords={}",
                    userId, userMessages.size(), topWords.size());

            return WordCloudResponse.of(topWords, userMessages.size());

        } catch (Exception e) {
            log.error("개인 사용자 질문 워드클라우드 생성 중 오류: userId={}, error={}", userId, e.getMessage(), e);
            return WordCloudResponse.empty();
        }
    }

    /**
     * 카테고리별 질문 워드클라우드 생성
     */
    public WordCloudResponse getCategoryQuestionsWordCloud(String categoryStr, Integer maxWords) {
        log.info("카테고리별 질문 워드클라우드 생성 시작: category={}, maxWords={}", categoryStr, maxWords);

        try {
            QuestionCategory category = QuestionCategory.valueOf(categoryStr.toUpperCase());

            // 모든 대화 조회
            List<ConversationDocument> allConversations = conversationRepository.findAll();

            // 해당 카테고리의 사용자 메시지만 추출
            List<String> categoryMessages = new ArrayList<>();
            for (ConversationDocument conversation : allConversations) {
                if (conversation.getMessages() != null) {
                    for (ConversationDocument.MessageDocument message : conversation.getMessages()) {
                        if (message.getSenderType() == SenderType.USER &&
                                category.equals(message.getCategory())) {
                            String messageText = message.getMessageText();
                            if (messageText != null && !messageText.trim().isEmpty()) {
                                categoryMessages.add(messageText.trim());
                            }
                        }
                    }
                }
            }

            if (categoryMessages.isEmpty()) {
                log.warn("카테고리 {}에 해당하는 메시지가 없음", category);
                return WordCloudResponse.empty();
            }

            // 단어 빈도수 계산
            Map<String, Integer> wordCounts = calculateWordFrequency(categoryMessages);

            // 상위 N개 단어 선택
            List<WordCloudResponse.WordCloudWord> topWords = selectTopWords(wordCounts, maxWords);

            log.info("카테고리별 질문 워드클라우드 생성 완료: category={}, totalMessages={}, uniqueWords={}",
                    category, categoryMessages.size(), topWords.size());

            return WordCloudResponse.of(topWords, categoryMessages.size());

        } catch (IllegalArgumentException e) {
            log.error("잘못된 카테고리 값: categoryStr={}", categoryStr);
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다: " + categoryStr);
        } catch (Exception e) {
            log.error("카테고리별 질문 워드클라우드 생성 중 오류: category={}, error={}", categoryStr, e.getMessage(), e);
            return WordCloudResponse.empty();
        }
    }

    /**
     * 대화 목록에서 사용자 메시지만 추출
     */
    private List<String> extractUserMessages(List<ConversationDocument> conversations) {
        List<String> userMessages = new ArrayList<>();

        for (ConversationDocument conversation : conversations) {
            if (conversation.getMessages() != null) {
                for (ConversationDocument.MessageDocument message : conversation.getMessages()) {
                    if (message.getSenderType() == SenderType.USER) {
                        String messageText = message.getMessageText();
                        if (messageText != null && !messageText.trim().isEmpty()) {
                            userMessages.add(messageText.trim());
                        }
                    }
                }
            }
        }

        return userMessages;
    }

    /**
     * 메시지 목록에서 단어 빈도수 계산
     */
    private Map<String, Integer> calculateWordFrequency(List<String> messages) {
        Map<String, Integer> wordCounts = new HashMap<>();

        for (String message : messages) {
            // 🔥 개선된 단어 추출 로직 사용
            List<String> words = extractWords(message);

            // 각 단어의 빈도수 계산
            for (String word : words) {
                if (isValidWord(word)) {
                    wordCounts.merge(word, 1, Integer::sum);
                }
            }
        }

        return wordCounts;
    }

    /**
     * 🔥 대폭 개선된 단어 추출 로직
     */
    private List<String> extractWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        // 1단계: 기본 텍스트 정리
        String cleanedText = preprocessText(text);

        // 2단계: 단어 분리
        String[] rawWords = cleanedText.split("\\s+");

        // 3단계: 각 단어별 정제 및 필터링
        List<String> validWords = new ArrayList<>();

        for (String word : rawWords) {
            String processedWord = processWord(word);
            if (processedWord != null && isValidWord(processedWord)) {
                validWords.add(processedWord);
            }
        }

        return validWords;
    }

    /**
     * 🔥 텍스트 전처리
     */
    private String preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?~`]", " ")  // 특수문자 제거
                .replaceAll("[ㅋㅎㅜㅠㅡㅂㄷㄱ]+", " ")                           // 무의미한 자음/모음 제거
                .replaceAll("\\d+", " ")                                        // 숫자 제거
                .replaceAll("\\s+", " ")                                        // 연속 공백 정리
                .trim();
    }

    /**
     * 🔥 개별 단어 처리
     */
    private String processWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return null;
        }

        word = word.trim();

        // 길이 필터링
        if (word.length() < 2 || word.length() > 20) {
            return null;
        }

        // 무의미한 패턴 제거
        if (MEANINGLESS_PATTERN.matcher(word).matches()) {
            return null;
        }

        // 한글 어미 제거 시도
        String stemmed = removeKoreanEndings(word);

        return stemmed;
    }

    /**
     * 🔥 한글 어미 제거 (간단한 형태소 분석)
     */
    private String removeKoreanEndings(String word) {
        if (word.length() < 3) {
            return word;
        }

        // 일반적인 어미들 제거
        String[] endings = {
                "에요", "이에요", "예요", "이예요", "습니다", "입니다",
                "해요", "해", "하다", "하는", "한", "할", "했", "하고",
                "이다", "이야", "야", "아", "어", "지", "죠", "요",
                "을까", "을까요", "까", "까요", "네요", "네", "고요",
                "거야", "거예요", "이죠", "죠", "잖아", "잖아요"
        };

        for (String ending : endings) {
            if (word.endsWith(ending) && word.length() > ending.length() + 1) {
                return word.substring(0, word.length() - ending.length());
            }
        }

        return word;
    }

    /**
     * 🔥 대폭 강화된 단어 유효성 검사
     */
    private boolean isValidWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        word = word.trim().toLowerCase();

        // 1. 길이 검사
        if (word.length() < 2 || word.length() > 15) {
            return false;
        }

        // 2. 불용어 검사
        if (STOP_WORDS.contains(word)) {
            return false;
        }

        // 3. 의미있는 키워드인지 확인
        if (MEANINGFUL_KEYWORDS.contains(word)) {
            return true;  // 화이트리스트에 있으면 바로 통과
        }

        // 4. 문자 구성 검사
        if (!MIXED_VALID.matcher(word).matches()) {
            return false;
        }

        // 5. 영어 단어는 최소 3글자 이상
        if (ENGLISH_ONLY.matcher(word).matches() && word.length() < 3) {
            return false;
        }

        // 6. 한글 단어는 최소 2글자 이상
        if (KOREAN_ONLY.matcher(word).matches() && word.length() < 2) {
            return false;
        }

        // 7. 반복 문자 검사 (예: "ㅋㅋㅋ", "아아아")
        if (hasRepeatingChars(word)) {
            return false;
        }

        // 8. 의미 없는 영어 단어 필터링
        if (ENGLISH_ONLY.matcher(word).matches() && isNonMeaningfulEnglish(word)) {
            return false;
        }

        // 9. 한글 조사/어미만으로 이루어진 단어 제거
        if (KOREAN_ONLY.matcher(word).matches() && isOnlyParticleOrEnding(word)) {
            return false;
        }

        return true;
    }

    /**
     * 반복 문자 검사
     */
    private boolean hasRepeatingChars(String word) {
        if (word.length() < 3) return false;

        char firstChar = word.charAt(0);
        int repeatCount = 1;

        for (int i = 1; i < word.length(); i++) {
            if (word.charAt(i) == firstChar) {
                repeatCount++;
                if (repeatCount >= 3) {
                    return true;
                }
            } else {
                break;
            }
        }

        return false;
    }

    /**
     * 의미 없는 영어 단어 필터링
     */
    private boolean isNonMeaningfulEnglish(String word) {
        String[] meaninglessEnglish = {
                "the", "and", "but", "for", "with", "from", "what", "when", "where", "why", "how",
                "yes", "yep", "nope", "okay", "omg", "lol", "wtf", "asap", "fyi", "btw", "idk",
                "abc", "xyz", "test", "hello", "hi", "hey", "bye", "good", "bad", "very", "much",
                "pdp", "zz", "zzz"  // 실제 데이터에서 발견된 의미없는 단어들
        };

        return Arrays.asList(meaninglessEnglish).contains(word.toLowerCase());
    }

    /**
     * 한글 조사/어미만인지 검사
     */
    private boolean isOnlyParticleOrEnding(String word) {
        String[] particlesAndEndings = {
                "이", "가", "을", "를", "에", "에서", "로", "으로", "와", "과", "의", "도", "만", "은", "는",
                "해", "하", "지", "고", "어", "아", "네", "요", "죠", "야", "까", "까요", "어요", "아요"
        };

        return Arrays.asList(particlesAndEndings).contains(word);
    }

    /**
     * 빈도수 기준으로 상위 N개 단어 선택
     */
    private List<WordCloudResponse.WordCloudWord> selectTopWords(Map<String, Integer> wordCounts, Integer maxWords) {
        return wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())  // 빈도수 내림차순
                .limit(maxWords)
                .map(entry -> WordCloudResponse.WordCloudWord.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}