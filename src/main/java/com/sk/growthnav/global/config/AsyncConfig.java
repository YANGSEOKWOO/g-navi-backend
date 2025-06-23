package com.sk.growthnav.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 썸네일 추출을 위한 전용 스레드 풀
     */
    @Bean(name = "thumbnailExecutor")
    public Executor thumbnailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 코어 스레드 수: 2개
        executor.setCorePoolSize(2);

        // 최대 스레드 수: 4개
        executor.setMaxPoolSize(4);

        // 큐 용량: 100개
        executor.setQueueCapacity(100);

        // 스레드 이름 접두사
        executor.setThreadNamePrefix("thumbnail-");

        // 큐가 가득 찰 때 정책: 호출자 스레드에서 실행
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 애플리케이션 종료 시 처리
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 스레드 풀 초기화
        executor.initialize();

        log.info("썸네일 추출용 스레드 풀 설정 완료: corePoolSize=2, maxPoolSize=4, queueCapacity=100");

        return executor;
    }

    /**
     * 일반적인 비동기 작업을 위한 스레드 풀
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 코어 스레드 수: 5개
        executor.setCorePoolSize(5);

        // 최대 스레드 수: 10개
        executor.setMaxPoolSize(10);

        // 큐 용량: 200개
        executor.setQueueCapacity(200);

        // 스레드 이름 접두사
        executor.setThreadNamePrefix("async-");

        // 큐가 가득 찰 때 정책: 호출자 스레드에서 실행
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 애플리케이션 종료 시 처리
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // 스레드 풀 초기화
        executor.initialize();

        log.info("일반 비동기 작업용 스레드 풀 설정 완료: corePoolSize=5, maxPoolSize=10, queueCapacity=200");

        return executor;
    }
}