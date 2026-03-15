package com.haconaka.demo.schedule;

import com.haconaka.demo.config.RestartConfig;
import com.haconaka.demo.service.youtube.YoutubeContentService;
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
    private final YoutubeContentService youtubeContentService;

    // 3분 단위 체크로 10분 재서 스케쥴링 멈추기
    @Scheduled(cron = "5 0/3 * * * *")
    public void deleteLiveStreamsWithTimer() {
        restartConfig.deleteLiveStreamWithTenMinutesTimer();
    }

    // 서버 시작 30초 후 구독갱신 로직
    @Scheduled(initialDelay = 30 * 1000)
    public void renewAllSubscriptionsOnInit() {
        youtubeSubscriptionService.subscribeAllYtChannels();
    }

    // 매일 00시 05분 마다 (1일 1회) 구독갱신
    @Scheduled(cron = "0 5 0 * * *")
    public void renewAllSubscriptionsDaily() {
        youtubeSubscriptionService.subscribeAllYtChannels();
    }

    // 매일 오전 00시 15분에 각 채널 상위 50건을 조회하여 아카이브 갱신
    @Scheduled(cron = "0 15 0 * * *")
    public void insertArchiveDaily() {
        // true : AllArchive / false : 각 채널마다 50건 / 기본적으로 false입니다.
        youtubeContentService.insertAllArchive(false);
    }
}
