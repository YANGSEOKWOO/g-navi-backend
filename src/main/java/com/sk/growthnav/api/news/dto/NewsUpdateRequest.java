package com.sk.growthnav.api.news.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsUpdateRequest {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;  // null이면 URL에서 자동 추출

    @Size(max = 500, message = "URL은 500자 이하여야 합니다.")
    private String url;

    // 제목이 제공되었는지 확인
    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }
}