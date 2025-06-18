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
    private String expert;
    private String status;  // "승인 대기", "승인됨", "거부됨"
    private String url;
    private String date;

    // Admin 관리 페이지용 추가 필드
    private Boolean canApprove;    // 승인 가능 여부
    private Boolean canUnapprove;  // 승인 해제 가능 여부
    private Boolean canReject;     // 거부 가능 여부

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Admin용 - 관리 버튼 포함
    public static NewsResponse from(News news) {
        return NewsResponse.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .expert(news.getExpert().getName())
                .status(news.getStatus().getDescription())
                .url(news.getUrl())
                .date(news.getCreatedAt().format(DATE_FORMATTER))
                .canApprove(news.isPending())     // 대기중일 때만 승인 가능
                .canUnapprove(news.isApproved())  // 승인됨일 때만 해제 가능
                .canReject(!news.isRejected())    // 거부됨이 아닐 때만 거부 가능
                .build();
    }

    // 일반 사용자용 - 관리 버튼 없음
    public static NewsResponse forPublic(News news) {
        return NewsResponse.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .expert(news.getExpert().getName())
                .url(news.getUrl())
                .date(news.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }
}