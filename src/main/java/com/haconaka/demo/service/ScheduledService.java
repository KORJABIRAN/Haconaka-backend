package com.haconaka.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@EnableScheduling
@Service
@RequiredArgsConstructor
public class ScheduledService {

    private volatile boolean isDatabaseEmpty = false;
    private LocalDateTime startEmpty = null;
    private volatile boolean isScheduleDisabled = false;
    private final YoutubePubSubService youtubePubSubService;
    private final YoutubeSubscriptionService youtubeSubscriptionService;

    // 3분 단위 체크로 10분 재서 스케쥴링 멈추기
    @Scheduled(cron = "5 0/3 * * * *")
    public void scheduledRefresh() {
        // 너 10분 넘었어? 돌아가.
        if (isScheduleDisabled) {
            return;
        }

        // 스케쥴링수행 및 DB사이즈값 리턴
        int valueSize = youtubePubSubService.refreshAllCurrent();

        LocalDateTime now = LocalDateTime.now();

        // 조회해 보니 total == 0 이네?
        if (valueSize == 0) {
            // 심지어 얘가 DB에 값이 있다고 착각하고있네?
            if (!isDatabaseEmpty) {
                isDatabaseEmpty = true;
                startEmpty = now;
                log.info("데이터베이스가 비었습니다. 지금부터 10분을 세겠습니다.");
            } else if (startEmpty != null) {
                // DB가 0인거 이미 알고있었는데? 몇시에 0이었는지도 아는데? 그러니까 몇분 지났는지 시간비교해.
                Duration duration = Duration.between(startEmpty, now);
                if (duration.toMinutes() >= 10) {
                    //10분 넘었네?? 다음 부터는 멈춰!
                    isScheduleDisabled = true;
                    log.info("10분이 초과하였으므로 스케쥴링을 중지합니다.");
                } else {
                    log.info("아직 10분이 지나지 않았습니다.");
                }
            }
        } else { // DB에 값이 0이었지만 새로 생겼어. 스케쥴링 살려내!
            youtubePubSubService.restartSchedule();
        }
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
