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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordCloudService {

    private final ConversationRepository conversationRepository;
    private final MemberRepository memberRepository;

    // ✅ 중복 제거된 한국어 불용어 목록
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            // 조사, 어미
            "이", "가", "을", "를", "에", "에서", "와", "과", "의", "로", "으로", "부터", "까지", "에게", "한테",
            "은", "는", "도", "만", "라도", "이나", "나", "든지", "이든지", "라든지", "이라도",
            "하고", "하며", "이며", "며", "고", "지만", "습니다", "입니다", "있습니다", "됩니다",

            // 대명사, 지시어
            "이것", "그것", "저것", "여기", "거기", "저기", "이거", "그거", "저거", "이런", "그런", "저런",
            "이렇게", "그렇게", "저렇게", "어디", "언제", "무엇", "누구", "왜", "어떻게", "얼마나",

            // 일반적인 단어 (중복 "일" 제거)
            "있다", "없다", "이다", "되다", "하다", "같다", "다르다", "좋다", "나쁘다", "크다", "작다",
            "많다", "적다", "높다", "낮다", "빠르다", "느리다", "새롭다", "오래되다",

            // 시간 관련
            "오늘", "어제", "내일", "지금", "현재", "나중에", "먼저", "다음", "이전", "요즘", "최근",

            // 기타 일반적인 단어
            "정말", "진짜", "아주", "매우", "너무", "조금", "많이", "잘", "못", "안", "또", "그리고",
            "하지만", "그러나", "그래서", "따라서", "때문에", "위해", "위한", "통해", "대해", "관해",
            "사람", "것", "일", "때", "곳", "점", "면", "경우", "상황", "문제", "방법", "결과",

            // 질문 관련 불용어
            "질문", "궁금", "알고", "싶습니다", "싶어요", "해주세요", "알려주세요", "도움", "부탁", "문의",
            "요청", "설명", "답변", "추천", "의견", "생각", "조언",

            // 단위, 수량
            "개", "명", "번", "차", "년", "월", "시", "분", "초", "원", "달러", "퍼센트",

            // 접속사, 부사
            "그럼", "그러면", "만약", "혹시", "아마", "아마도", "확실히", "분명히", "당연히", "물론"
    ));

    @PostConstruct
    public void init() {
        log.info("WordCloudService 초기화 완료");
        log.info("불용어 개수: {}", STOP_WORDS.size());
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
            // 메시지를 단어로 분리
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
     * 텍스트에서 의미있는 단어 추출
     */
    private List<String> extractWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        // 기본적인 텍스트 정리
        String cleanedText = text
                .replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")  // 한글, 영문, 숫자, 공백만 유지
                .replaceAll("\\s+", " ")                     // 연속 공백을 하나로
                .trim()
                .toLowerCase();

        // 공백으로 단어 분리
        String[] words = cleanedText.split("\\s+");

        return Arrays.asList(words);
    }

    /**
     * 유효한 단어인지 검사 (불용어 제거, 길이 검사)
     */
    private boolean isValidWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }

        word = word.trim().toLowerCase();

        // 길이 검사 (2글자 이상)
        if (word.length() < 2) {
            return false;
        }

        // 숫자만으로 이루어진 단어 제외
        if (word.matches("\\d+")) {
            return false;
        }

        // 불용어 제거
        if (STOP_WORDS.contains(word)) {
            return false;
        }

        // 한글 또는 영문이 포함된 단어만 허용
        return word.matches(".*[가-힣a-zA-Z]+.*");
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