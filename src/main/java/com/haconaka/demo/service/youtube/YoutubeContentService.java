package com.haconaka.demo.service.youtube;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.haconaka.demo.config.CurrentDateTime;
import com.haconaka.demo.dto.PubSubNotificationDto;
import com.haconaka.demo.entity.HacoAddressEntity;
import com.haconaka.demo.entity.HacoCurrentLivestreamEntity;
import com.haconaka.demo.repository.HacoAddressRepository;
import com.haconaka.demo.repository.HacoCurrentLivestreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@EnableScheduling
@Service
@RequiredArgsConstructor
public class YoutubeContentService {

    private final HacoCurrentLivestreamRepository currentRepo;
    private final HacoAddressRepository addressRepo;
    private final YoutubeApiService youtubeApi;
    private final XmlParsingService xmlParsingService;

    private final CurrentDateTime currentDateTime;

    // 이름만 그럴듯한 껍데기!
    public void handleNotification(String atomXml) {
        insertLiveStream(atomXml);
    }

    // LiveStream INSERT 로직
    public void insertLiveStream (String atomXml) {
        try {
            // TODO:데이터가1건이 아닌경우에 대해 예외처리를 할 필요가 있음.
            // 여기서는 pubSub으로 들어온 요청이 무조건 1개라고 가정합니다.
            PubSubNotificationDto pubSubData = xmlParsingService.parseAtomXml(atomXml).get(0);
            String channelId = pubSubData.getChannelId();
            String videoId = pubSubData.getVideoId();
            log.info("==================== Log Start : new request from youtube PubSub");
            log.info("{} - A new request!", currentDateTime.getCurrentDateTime());
            log.info("channelID : {} / videoID : {} / title : {}", channelId, videoId, pubSubData.getTitle());

            List<String> videoIds = new ArrayList<>();
            videoIds.add(videoId);

            // 예외처리1. 채널ID, 비디오ID 둘 중 하나라도 없으면? 즉시 종료.
            if (channelId == null || videoId == null) {
                log.warn("Data Integrity Error : channelId or videoId is not found. finish process now.");
                return;
            }

            // 예외처리2. videoId로 api검색해서 상태가 live가 아니면? 즉시 종료.
            // 여기서는 List값이 무조건 1개 라고 가정합니다.
            // TODO:데이터가1건이 아닌경우에 대해 예외처리를 할 필요가 있음.
            String status = youtubeApi.getYoutubeStatusByVideoId(videoIds).stream()
                    .map(Video::getSnippet)
                    .map(VideoSnippet::getLiveBroadcastContent)
                    .toList().get(0);
            if (!"live".equals(status)) {
                log.info("status is not live. finish process now.");
                return;
            }

            // 예외처리3. liveStream 테이블을 videoID로 찾아보니 이미 정보가 있어? 즉시 종료.
            if (currentRepo.findByAddress(videoId) != null) {
                log.warn("Data Integrity Error : Failed to save LiveStream : Data already present.");
                return;
            }

            // 채널id로 address 테이블 get해서 memberPk 취득
            Integer memberPk = Optional.ofNullable(addressRepo.findByAddress(channelId)).orElseGet(() -> {
                log.warn("Data Integrity Error : memberPK is not found. return 0 and finish process now.");
                return new HacoAddressEntity();
            }).getMemberPk();
            if (memberPk == 0) return; // 예외처리4. 멤버PK 못찾았어? 즉시 종료.

            // 이제 memberPk를 취득했으니 videoId랑 같이 Livestream 테이블에 저장
            currentRepo.save(HacoCurrentLivestreamEntity.builder()
                    .memberPk(memberPk)
                    .address(videoId)
                    .build());
            log.info(currentDateTime.getCurrentDateTime() + " - succeed save.");
        } catch (Exception e) {
            log.error("Exception : Failed to handle notification");
        } finally {
            log.info("==================== Log End : new request from youtube PubSub");
        }
    }

    // LiveStream DELETE 로직 (스케쥴로 3분마다 돌아감)
    public void deleteLiveStream() {
        log.info("==================== Log start : update livestreaming information");
        log.info("{} - Start update livestreaming information", currentDateTime.getCurrentDateTime());
        List<HacoCurrentLivestreamEntity> livestreamEntities = currentRepo.findAll();
        List<String> videoIds = livestreamEntities.stream().
                map(HacoCurrentLivestreamEntity::getAddress).toList();

        // 삭제조건1. DB내에 address가 2건 이상인 경우 (livestreamEntities를 이용한다.)
        List<HacoCurrentLivestreamEntity> entitiesToDelete = livestreamEntities.stream()
                .collect(Collectors.groupingBy(HacoCurrentLivestreamEntity::getAddress))
                .values().stream()
                .filter(group -> group.size() > 1)
                .flatMap(group -> group.stream().skip(1))
                .collect(Collectors.toList());

        if (!entitiesToDelete.isEmpty()) {
            currentRepo.deleteAll(entitiesToDelete);
            log.info("중복된 address 데이터 {}건을 삭제합니다.", entitiesToDelete.size());
        }

        // 삭제조건2. status != live
        // 루프 돌리며 취득한 videoIds로 youtube API 실행
        // 주의! 얘는 무조건 1건 아님! 복수건일수 있음!
        // TODO: 얘도 가만보니까 루프로 정보만 따오고 요청은 한번만 가능할거같긴한데... 그럼 로그가 이상해지나? 아닌거같은데
        List<Video> videos = youtubeApi.getYoutubeStatusByVideoId(videoIds);
        for (Video video : videos) {
            String result;
            String status = video.getSnippet().getLiveBroadcastContent();
            String channelTitle = video.getSnippet().getChannelTitle();
            String videoId = video.getId();
            if (!"live".equals(status)) {
                currentRepo.delete(currentRepo.findByAddress(video.getId()));
                result = "DELETE";
            } else {
                result = "KEEP";
            }
            log.info("result : {} / status : {} / ChannelID : {} / videoId : {}", result, status, channelTitle, videoId);
        }
        // TODO : 삭제조건3 으로 비공개동영상도 추가해야될거같은데, 일단 비공개동영상의 리스폰스가 어떻게 생겨먹었는지 부터 알아야될거같음.
        log.info("{} - finished update livestreaming information", currentDateTime.getCurrentDateTime());
        log.info("==================== Log End : update livestreaming information");
    }
}
