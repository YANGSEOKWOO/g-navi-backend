package com.sk.growthnav.api.news.repository;

import com.sk.growthnav.api.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    // 등록된 뉴스만 조회 (일반 사용자용)
    List<News> findByIsRegisteredTrueOrderByCreatedAtDesc();

    // 모든 뉴스 조회 (관리자용)
    List<News> findAllByOrderByCreatedAtDesc();

    // 특정 작성자의 뉴스 조회
    List<News> findByWriterIdOrderByCreatedAtDesc(Long writerId);

    // 제목으로 검색
    List<News> findByTitleContainingAndIsRegisteredTrueOrderByCreatedAtDesc(String title);
}
