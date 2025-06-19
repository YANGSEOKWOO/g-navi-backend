package com.sk.growthnav.api.news.entity;

import com.sk.growthnav.api.member.entity.Member;
import com.sk.growthnav.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String url;

    // 썸네일 이미지 경로 추가
    @Column(length = 500)
    private String thumbnailPath;  // PVC 내 이미지 파일 경로

    // 썸네일 접근 URL 추가
    @Column(length = 500)
    private String thumbnailUrl;   // 사용자가 접근할 수 있는 URL

    // isRegistered 대신 status 사용
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NewsStatus status = NewsStatus.PENDING;  // 기본값: 승인 대기

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Member expert; // EXPERT 또는 Admin

    // 수정 메서드
    public void updateNews(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // 썸네일 설정 메서드
    public void setThumbnail(String thumbnailPath, String thumbnailUrl) {
        this.thumbnailPath = thumbnailPath;
        this.thumbnailUrl = thumbnailUrl;
    }

    // 승인 처리
    public void approve() {
        this.status = NewsStatus.APPROVED;
    }

    // 승인 해제 (등록된 기사를 다시 대기상태로)
    public void unapprove() {
        this.status = NewsStatus.PENDING;
    }

    // 거부/삭제 처리
    public void reject() {
        this.status = NewsStatus.REJECTED;
    }

    // 상태 확인 메서드들
    public boolean isApproved() {
        return this.status == NewsStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == NewsStatus.PENDING;
    }

    public boolean isRejected() {
        return this.status == NewsStatus.REJECTED;
    }
}