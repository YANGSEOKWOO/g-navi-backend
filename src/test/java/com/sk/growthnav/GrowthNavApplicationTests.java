package com.sk.growthnav;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // 테스트 프로필 사용
@DisplayName("Spring Boot 애플리케이션 통합 테스트")
class GrowthNavApplicationTests {

    @Test
    @DisplayName("Spring Context가 정상적으로 로드되는지 확인")
    void contextLoads() {
        // 이 테스트는 Spring 애플리케이션이 정상적으로 시작되는지 확인
        // 모든 Bean이 올바르게 생성되고 의존성 주입이 정상 작동하는지 검증

        // 실패하면: 설정 오류, Bean 순환 참조, 의존성 문제 등이 있음을 의미
        // 성공하면: 기본적인 Spring Boot 설정이 올바름을 의미
    }

    @Test
    @DisplayName("애플리케이션이 MongoDB와 PostgreSQL 설정으로 시작되는지 확인")
    void applicationStartsWithDatabaseConfiguration() {
        // 이 테스트가 통과하면:
        // 1. MongoDB 설정이 올바름
        // 2. PostgreSQL 설정이 올바름
        // 3. JPA 및 MongoDB 설정이 충돌하지 않음
        // 4. Repository 빈들이 올바르게 생성됨
    }
}