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

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRegisterd = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer; // Writer 또는 Admin

    // 수정 메서드
    public void updateNews(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // 등록 상태 변경
    public void toggleRegistration() {
        this.isRegisterd = !this.isRegisterd;
    }
}
