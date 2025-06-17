package com.sk.growthnav.api.news.dto;

import com.sk.growthnav.api.news.entity.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsResponse {
    private Long newsId;
    private String title;
    private String writer;
    private Boolean isRegistered;
    private String url;
    private String date;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static NewsResponse from(News news) {
        return NewsResponse.builder()
                .newsId(news.getId())
                .writer(news.getWriter().getName())
                .title(news.getTitle())
                .isRegistered(news.getIsRegistered())
                .url(news.getUrl())
                .date(news.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }
}
