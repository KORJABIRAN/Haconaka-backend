package com.haconaka.demo.config;

import com.haconaka.demo.repository.livestream.LivestreamRepo;
import com.haconaka.demo.service.youtube.YoutubeContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestartConfig {

    private final YoutubeContentService youtubeContentService;
    private final LivestreamRepo livestreamRepo;

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

    // 에라모르겠다. 걍 여기다 넣어. 10분 동안 변화 없으면 로깅이 자동 종료되는 수동로직!!
    public void deleteLiveStreamWithTenMinutesTimer() {
        // 너 10분 넘었어? 돌아가.
        if (isScheduleDisabled) {
            return;
        }

        // 스케쥴링수행 및 DB사이즈값 리턴
        youtubeContentService.deleteLiveStream();

        LocalDateTime now = LocalDateTime.now();

        long valueSize = livestreamRepo.count();

        // 조회해 보니 total == 0 이네?
        if (valueSize == 0) {
            // 심지어 얘가 DB가 0건이 아니라고 착각하고있네?
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
            restartSchedule();
        }
    }
}
