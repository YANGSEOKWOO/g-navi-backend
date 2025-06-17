package com.sk.growthnav.api.news.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsManageRequest {

    @NotNull(message = "뉴스 ID는 필수입니다.")
    private Long newsId;

    @NotNull(message = "관리자 ID는 필수입니다.")
    private Long adminId;

    @NotNull(message = "관리 액션은 필수입니다.")
    private ManageAction action;

    public enum ManageAction {
        APPROVE("승인"),
        UNAPPROVE("승인 해제"),
        REJECT("거부");

        private final String description;

        ManageAction(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}