package com.haconaka.demo.service.schedule;

import com.haconaka.demo.config.RestartConfig;
import com.haconaka.demo.service.youtube.YoutubeSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@EnableScheduling
@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final YoutubeSubscriptionService youtubeSubscriptionService;
    private final RestartConfig restartConfig;

    // 3분 단위 체크로 10분 재서 스케쥴링 멈추기
    @Scheduled(cron = "5 0/3 * * * *")
    public void scheduledDeleteLiveStreams() {
        restartConfig.deleteLiveStreamWithTenMinutesTimer();
    }

    // 서버 시작 30초 후 구독갱신 로직
    @Scheduled(initialDelay = 30 * 1000)
    public void scheduledTask() {
        youtubeSubscriptionService.subscribeAllYtChannels();
    }

    // 매일 00시 05분 마다 (1일 1회) 구독갱신
    @Scheduled(cron = "0 5 0 * * *")
    public void scheduledTask2() {
        youtubeSubscriptionService.subscribeAllYtChannels();
    }
}
