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

    @NotBlank(message = "URL은 필수입니다.")
    @Size(max = 500, message = "URL은 500자 이하여야 합니다.")
    private String url;
}
