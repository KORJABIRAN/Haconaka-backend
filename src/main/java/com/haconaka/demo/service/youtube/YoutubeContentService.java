package com.haconaka.demo.service.youtube;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.haconaka.demo.config.CurrentDateTime;
import com.haconaka.demo.dto.PubSubNotificationDto;
import com.haconaka.demo.entity.ArchiveEntity;
import com.haconaka.demo.entity.LiveStreamEntity;
import com.haconaka.demo.entity.MemberEntity;
import com.haconaka.demo.repository.archive.ArchiveRepo;
import com.haconaka.demo.repository.livestream.LivestreamRepo;
import com.haconaka.demo.repository.member.MemberRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@EnableScheduling
@Service
@RequiredArgsConstructor
public class YoutubeContentService {

    private final LivestreamRepo livestreamRepo;
    private final MemberRepo memberRepo;
    private final ArchiveRepo archiveRepo;

    private final YoutubeApiService youtubeApi;
    private final XmlParsingService xmlParsingService;

    private final CurrentDateTime currentDateTime;

    // 이름만 그럴듯한 껍데기! (INSERT가 들어있어요)
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
            if (livestreamRepo.findByVideoId(videoId) != null) {
                log.warn("Data Integrity Error : Failed to save LiveStream : Data already present.");
                return;
            }

            // 채널id로 address 테이블 get해서 memberId 취득
            MemberEntity member = Optional.ofNullable(memberRepo.findByYoutubeChannelId(channelId)).orElseGet(() -> {
                log.warn("Data Integrity Error : memberPK is not found. return 0 and finish process now.");
                return new MemberEntity();
            });
            if (member.getId() == 0) return; // 예외처리4. 멤버PK 못찾았어? 즉시 종료.

            // 이제 memberPk를 취득했으니 videoId랑 같이 Livestream 테이블에 저장
            livestreamRepo.save(LiveStreamEntity.builder()
                    .member(member)
                    .videoId(videoId)
                    .build());
            log.info("{} - succeed save.", currentDateTime.getCurrentDateTime());
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
        List<LiveStreamEntity> livestreamEntities = livestreamRepo.findAll();
        List<String> videoIds = livestreamEntities.stream().
                map(LiveStreamEntity::getVideoId).toList();

        // 삭제조건1. DB내에 address가 2건 이상인 경우 (livestreamEntities를 이용한다.)
        List<LiveStreamEntity> entitiesToDelete = livestreamEntities.stream()
                .collect(Collectors.groupingBy(LiveStreamEntity::getVideoId))
                .values().stream()
                .filter(group -> group.size() > 1)
                .flatMap(group -> group.stream().skip(1))
                .collect(Collectors.toList());

        if (!entitiesToDelete.isEmpty()) {
            livestreamRepo.deleteAll(entitiesToDelete);
            log.info("중복된 address 데이터 {}건을 삭제합니다.", entitiesToDelete.size());
        }

        // 삭제조건2. status != live
        // 루프 돌리며 취득한 videoIds로 youtube API 실행
        // 주의! 얘는 무조건 1건 아님! 복수건일수 있음!
        // TODO: 얘도 가만보니까 루프로 정보만 따오고 요청은 한번만 가능할거같긴한데... 그럼 로그가 이상해지나? 아닌거같은데
        List<Video> videos = youtubeApi.getYoutubeStatusByVideoId(videoIds);
        if (!videos.isEmpty()) {
            for (Video video : videos) {
                String result;
                String status = video.getSnippet().getLiveBroadcastContent();
                String channelTitle = video.getSnippet().getChannelTitle();
                String videoId = video.getId();
                if (!"live".equals(status)) {
                    livestreamRepo.delete(livestreamRepo.findByVideoId(video.getId()));
                    result = "DELETE";
                } else {
                    result = "KEEP";
                }
                log.info("result : {} / status : {} / ChannelID : {} / videoId : {}", result, status, channelTitle, videoId);
            }
        }

        // TODO : 삭제조건3 으로 비공개동영상도 추가해야될거같은데, 일단 비공개동영상의 리스폰스가 어떻게 생겨먹었는지 부터 알아야될거같음.
        log.info("{} - finished update livestreaming information", currentDateTime.getCurrentDateTime());
        log.info("==================== Log End : update livestreaming information");
    }

    // Archive 테이블 All INSERT (약 2만건, 왠만하면 돌리는것을 자제합시다.)
    @Transactional
    public void insertAllArchive(boolean isAll) {
        log.info("===================== Log start : insert all archive");
        log.info("{} - Start insert all archive", currentDateTime.getCurrentDateTime());

        // 멤버 전체조회 후 Map화 (인덱싱 최적화) / Key: YoutubePlaylistId, Value: MemberEntity
        Map<String, MemberEntity> memberMap = memberRepo.findAll().stream()
                .collect(Collectors.toMap(
                        MemberEntity::getYoutubePlaylistId,
                        member -> member,
                        (existing, replacement) -> existing
                ));

        List<String> playlistIds = new ArrayList<>(memberMap.keySet());

        // Youtube Data API : 재생목록 내 전체 동영상 취득
        List<PlaylistItem> allPlaylistItems = youtubeApi.getYoutubeVideosInPlaylist(playlistIds, isAll);
        log.info("수집 완료. 총 비디오 개수: {}", allPlaylistItems.size());

        Set<String> existingVideoIds = new HashSet<>(archiveRepo.findAllVideoIds());

        // PlaylistItem -> ArchiveEntity 변환 (Stream 활용)
        List<ArchiveEntity> archiveEntities = allPlaylistItems.stream()
                .map(item -> {
                    String videoId = item.getContentDetails().getVideoId();

                    // [추가] 이미 DB에 존재하는 videoId라면 null 반환해서 걸러냄
                    if (existingVideoIds.contains(videoId)) {
                        return null;
                    }

                    String pId = item.getSnippet().getPlaylistId();
                    MemberEntity member = memberMap.get(pId);

                    if (member == null) return null;

                    // 섬네일 단계별 추출
                    ThumbnailDetails thumbnails = item.getSnippet().getThumbnails();
                    if (thumbnails == null) return null;
                    String thumbnailUrl = thumbnails.getDefault().getUrl();
                    if (thumbnails.getHigh() != null) thumbnailUrl = thumbnails.getHigh().getUrl();
                    if (thumbnails.getStandard() != null) thumbnailUrl = thumbnails.getStandard().getUrl();
                    if (thumbnails.getMaxres() != null) thumbnailUrl = thumbnails.getMaxres().getUrl();

                    return ArchiveEntity.builder()
                            .member(member)
                            .videoId(videoId)
                            .title(item.getSnippet().getTitle())
                            .thumbnail(thumbnailUrl)
                            .startAt(OffsetDateTime.parse(item.getSnippet().getPublishedAt().toString()))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        // 4. DB 저장 (Batch Insert 최적화)
        // 15,000건을 한 번에 넣기보다 1,000건씩 끊어서 저장하는 것을 추천 (선택 사항)
        if (!archiveEntities.isEmpty()) {
            archiveRepo.saveAll(archiveEntities);
            log.info("성공적으로 {}건의 아카이브를 저장했습니다.", archiveEntities.size());
        }

        log.info("{} - End insert all archive", currentDateTime.getCurrentDateTime());
    }
}
