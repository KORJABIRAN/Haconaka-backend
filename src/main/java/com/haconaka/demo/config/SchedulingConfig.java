package com.haconaka.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling // 스케줄링 기능 활성화 (이전 @Scheduled를 쓰지 않아도 @Async 등 다른 스케줄링 기능에 필요)
public class SchedulingConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); // 이 작업은 하나만 돌면 되므로 풀 사이즈를 1로 설정
        scheduler.setThreadNamePrefix("livestream-monitor-");
        scheduler.initialize(); // 스케줄러 초기화 필수
        return scheduler;
    }
}
