package com.haconaka.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class RestartConfig {

    private volatile boolean isDatabaseEmpty = false;
    private LocalDateTime startEmpty = null;
    private volatile boolean isScheduleDisabled = false;

    // 이 메서드 한방이면 스케쥴 살리가 OK
    public void restartSchedule() {
        isScheduleDisabled = false;
        isDatabaseEmpty = false;
        startEmpty = null;
        log.info("어떤 형태로든 변화를 감지하여 스케쥴을 다시 기동합니다.");
    }

}
