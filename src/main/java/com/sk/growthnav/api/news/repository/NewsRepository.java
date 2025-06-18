package com.sk.growthnav.api.news.repository;

import com.sk.growthnav.api.news.entity.News;
import com.sk.growthnav.api.news.entity.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    // 상태별 조회
    List<News> findByStatusOrderByCreatedAtDesc(NewsStatus status);

    // 모든 뉴스 조회 (관리자용)
    List<News> findAllByOrderByCreatedAtDesc();

    // 특정 작성자의 뉴스 조회
    List<News> findByExpertIdOrderByCreatedAtDesc(Long expertId);

    // 제목으로 검색 (승인된 것만)
    List<News> findByTitleContainingAndStatusOrderByCreatedAtDesc(String title, NewsStatus status);

    // 승인 대기중인 뉴스 개수
    long countByStatus(NewsStatus status);
}