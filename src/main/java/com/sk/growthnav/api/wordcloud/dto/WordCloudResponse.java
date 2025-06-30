package com.sk.growthnav.api.wordcloud.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WordCloudResponse {

    List<WordCloudWord> words;
    Integer totalWords;      // 고유 단어 개수
    Integer totalMessages;   // 분석된 총 메시지 수

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WordCloudWord {
        String text;    // 단어
        Integer count;  // 빈도수

        public static WordCloudWord of(String text, Integer count) {
            return WordCloudWord.builder()
                    .text(text)
                    .count(count)
                    .build();
        }
    }

    public static WordCloudResponse of(List<WordCloudWord> words, Integer totalMessages) {
        return WordCloudResponse.builder()
                .words(words)
                .totalWords(words.size())
                .totalMessages(totalMessages)
                .build();
    }

    // 빈 결과 생성
    public static WordCloudResponse empty() {
        return WordCloudResponse.builder()
                .words(List.of())
                .totalWords(0)
                .totalMessages(0)
                .build();
    }
}