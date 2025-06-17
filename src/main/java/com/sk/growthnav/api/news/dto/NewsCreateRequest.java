package com.sk.growthnav.api.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsCreateRequest {

    @NotNull(message = "작성자 Id는 필수입니다.")
    private Long writerId;

    // title을 선택사항으로 변경 (비어있으면 URL에서 자동 추출)
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;  // @NotBlank 제거

    @NotBlank(message = "URL은 필수입니다.")
    @Size(max = 500, message = "URL은 500자 이하여야 합니다.")
    private String url;
}
