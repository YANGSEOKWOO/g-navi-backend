package com.sk.growthnav.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")  // 테스트 환경 제외
public class DataInitializer {

    private final AdminInitializer adminInitializer;
    private final MockDataInitializer mockDataInitializer;

    @Value("${data.initialization.enabled:false}")
    private boolean dataInitEnabled;

    @Value("${data.initialization.mode:minimal}")
    private String dataInitMode;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void initializeData() {
        log.info("🚀 데이터 초기화 시작 - 환경: {}, 모드: {}", activeProfile, dataInitMode);

        // 1. 관리자 계정은 모든 환경에서 생성
        adminInitializer.initAdmin();

        // 2. 목업 데이터는 설정에 따라
        if (dataInitEnabled) {
            log.info("📦 목업 데이터 초기화 시작...");
            mockDataInitializer.initMockData(dataInitMode);
        } else {
            log.info("⏭️  목업 데이터 초기화 건너뜀 (disabled)");
        }

        log.info("✅ 데이터 초기화 완료!");
    }
}