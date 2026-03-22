package com.haconaka.demo.service.youtube;

import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoLiveStreamingDetails;
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
import org.springframework.scheduling.annotation.Async;
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

    // insertLiveStream - pubsub요청이 올때 도는 메서드
    public void handleNotification(String atomXml) {
        // 여기서는 pubSub으로 들어온 요청이 무조건 1개라고 가정합니다.
        try {
            PubSubNotificationDto pubSubData = xmlParsingService.parseAtomXml(atomXml).get(0);
            List<String> videoIds = List.of(pubSubData.getVideoId());
            // 본게임 시작
            insertLiveStream(videoIds);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    // insertLiveStream - 얘는 이제 스케쥴링 용도 입니다.
    public void insertLiveStreamWithDB() {
        // 모든 멤버의 채널ID를 취득합니다.
        List<String> channelIds = memberRepo.findAll().stream().map(MemberEntity::getYoutubeChannelId).toList();
        // Youtube Data API : 모든 멤버들의 액티비티 정보를 긁어옵니다. -> videoId만 긁어옵니다. -> 중복제거 위해 셋으로.
        List<String> activityVideoIds = youtubeApi.getVideoIdsByChannelIdFromActivity(channelIds)
                .stream().distinct().toList();
        // 본게임 시작
        insertLiveStream(activityVideoIds);
    }

    // LiveStream INSERT 로직
    private void insertLiveStream(List<String> videoIds) {
        try {
            // videoIds 를 Empty 체크 하지 않았습니다만, 귀찮은것도 맞는데 사실 Empty가 들어올 확률이 0%에 가까움.
            log.info("==================== Log Start : new request from youtube PubSub");
            log.info("{} - A new request!", currentDateTime.getCurrentDateTime());
            // 준비 - 레코드 하나 파서 channelId, videoId, title, liveStatus 4개 수집
            List<liveInsertDTO> liveInsertDTOS = youtubeApi.getYoutubeStatusByVideoId(videoIds).stream()
                    .map(data -> new liveInsertDTO(
                            data.getSnippet().getChannelId(),
                            data.getId(),
                            data.getSnippet().getTitle(),
                            data.getSnippet().getLiveBroadcastContent()
                    )).toList();

            // 준비 - 인덱싱 용도로 멤버map 만들기 / channelId 와 MemberEntity
            Map<String, MemberEntity> memberMap = memberRepo.findAll().stream()
                    .collect(Collectors.toMap(MemberEntity::getYoutubeChannelId, m -> m));

            // 준비 - 인덱싱 용도로 라이브ids 만들기 / liveVideoIds
            Set<String> liveVideoIds = livestreamRepo.findAll().stream()
                    .map(LiveStreamEntity::getVideoId).collect(Collectors.toSet());

            // 본격적인 예외처리의 시작. 살아남은 놈만 새로운 객체로 들어갈수 있다.
            List<LiveStreamEntity> result = new ArrayList<>();
            int insertCount = 0;
            for (liveInsertDTO data : liveInsertDTOS) {
                log.info("status : {} / channelID : {} / videoID : {} / title : {}",
                        data.liveStatus, data.channelId, data.videoId, data.title);
                // 예외처리1. videoId로 api검색해서 상태가 live가 아니면? 즉시 종료.
                if (!"live".equals(data.liveStatus)) {
                    log.info("status is not live. finish process now.");
                    continue;
                }

                // 예외처리2. 채널ID, 비디오ID 둘 중 하나라도 없으면? 즉시 종료.
                if (data.channelId == null || data.videoId == null) {
                    log.warn("Data Integrity Error : channelId or videoId is not found. finish process now.");
                    continue;
                }

                // 예외처리3. liveStream 테이블을 videoID로 찾아보니 이미 정보가 있어? 즉시 종료.
                // 값이 0임 -> isEmpty는 참임 -> 뒤집으니까 최종 false임
                // 값이 1임 -> isEmpty는 거짓임 -> 뒤집으니까 최종 true임 -> 이때가 중복인 거니까 종료해야함
                if (liveVideoIds.contains(data.videoId)) {
                    log.warn("Data Integrity Error : Failed to save LiveStream : Data already present.");
                    continue;
                }

                // 예외처리4. 채널id로 memberMap 인덱싱 -> memberId 체크 -> 멤버PK 못찾았어? 즉시 종료.
                MemberEntity member = memberMap.get(data.channelId);
                if (member == null) {
                    log.warn("Data Integrity Error : memberPK is not found. return 0 and finish process now.");
                    continue;
                }

                // 4가지 예외처리를 모두 살아남은 강한자가 새로운 객체에 들어갈것이다.
                result.add(LiveStreamEntity.builder()
                        .title(data.title)
                        .videoId(data.videoId)
                        .member(member)
                        .build());
                insertCount++;
            }

            // 진짜 다됐음. 이제 insert 하자
            if (!result.isEmpty()) livestreamRepo.saveAll(result);

            log.info("{} - 총 {}건 입력하였습니다", currentDateTime.getCurrentDateTime(), insertCount);
        } catch (Exception e) {
            log.error("치명적인 오류 : YoutubeContentService 클래스의 insertLiveStream 에서 catch 호출됨", e);
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
                    livestreamRepo.delete(livestreamRepo.findByVideoId(videoId));
                    result = "DELETE";
                } else {
                    result = "KEEP";
                }
                log.info("result : {} / status : {} / ChannelID : {} / videoId : {}", result, status, channelTitle, videoId);
            }
        }

        // 삭제조건3. 비공개,멤버한정전환,동영상삭제 등이 일어난 경우
        List<String> videoIdsAPI = videos.stream().map(Video::getId).toList();

        List<LiveStreamEntity> retiredItem = livestreamEntities.stream()
                .filter(data -> !videoIdsAPI.contains(data.getVideoId()))
                .toList();

        if (!retiredItem.isEmpty()) {
            livestreamRepo.deleteAll(retiredItem);
            log.info("비공개/멤버전환/삭제된 address 데이터 {}건을 삭제합니다.", retiredItem.size());
        }

        log.info("{} - finished update livestreaming information", currentDateTime.getCurrentDateTime());
        log.info("==================== Log End : update livestreaming information");
    }

    // Archive 테이블 All INSERT (약 2만건, 왠만하면 돌리는것을 자제합시다.)
    @Transactional
    @Async
    public void insertArchive(boolean isAll) {
        try {
            log.info("===================== Log start : insert all archive");
            log.info("{} - Start insert all archive", currentDateTime.getCurrentDateTime());

            // 쿼리 최소화 위해 1번만 변수에 담아 조회.
            List<MemberEntity> baseMembers = memberRepo.findAll();

            // 나~~~중에 가져다 쓸 인덱싱용 Map / Key: YoutubeChannelId, Value: MemberEntity
            Map<String, MemberEntity> channelIdAndMemberMap = baseMembers.stream()
                    .collect(Collectors.toMap(
                            MemberEntity::getYoutubeChannelId, member -> member));

            // 29명의 playlistIds 추출
            List<String> playlistIds = baseMembers.stream().map(MemberEntity::getYoutubePlaylistId).toList();

            // Youtube Data API : 재생목록 29개 내 전체 videoId 취득
            log.info("Part 1 : start - playlistIds로 videoIdsYoutube 취득");
            List<String> videoIdsYoutube = youtubeApi.getYoutubeVideoIdsInPlaylist(playlistIds, isAll);
            log.info("Part 1 : end - playlistIds로 videoIdsYoutube 취득. 총 ID 개수: {}", videoIdsYoutube.size());

            // 1만건의 videoIds로 중복인 DB 데이터를 긁어옴. 500건씩 끊어서 조회함. Map에 한번에 넣기.
            // 어쩔수 없이 2만건의 엔티티를 들고있는 유일한 값.
            log.info("Part 2 : start - {}건의 videoIds로 중복값을 DB에서 조회", videoIdsYoutube.size());
            Map<String, ArchiveEntity> updateMap = new HashMap<>();
            for (int i = 0; i < videoIdsYoutube.size(); i += 500) {
                int endIndex = Math.min(i + 500, videoIdsYoutube.size());
                List<String> videoIdsSize500 = videoIdsYoutube.subList(i, endIndex);
                updateMap.putAll(archiveRepo.findAllByVideoIdIn(videoIdsSize500).stream()
                        .collect(Collectors.toMap(ArchiveEntity::getVideoId, a -> a)));
                log.info("Part 2 : Processed: {} / {}", endIndex, videoIdsYoutube.size());
            }
            log.info("Part 2 : end - {}건의 videoIds로 중복값을 DB에서 조회. 총 중복 개수 : {}",
                    videoIdsYoutube.size(), updateMap.size());

            /////////////////// 여기부터 이제 500건씩 끊어서 작업합니다. ///////////////////////////////
            int insertCount = 0;
            int updateCount = 0;

            for (int i = 0; i < videoIdsYoutube.size(); i += 500) {
                int currentChunkEnd = Math.min(i + 500, videoIdsYoutube.size());

                // 1. Youtube Data API : 취득한 전체동영상의 videoIds 로 DetailList를 조회
                List<Video> videoDetail500 = new ArrayList<>();
                log.info("Part 3 : start - {} ~ {} 구간 detail 정보 취득", i, currentChunkEnd);
                for (int j = i; j < currentChunkEnd; j += 50) {
                    int endIndex = Math.min(j + 50, currentChunkEnd);
                    List<String> videoIdsSize50 = videoIdsYoutube.subList(j, endIndex);
                    videoDetail500.addAll(youtubeApi.getYoutubeStatusByVideoId(videoIdsSize50));
                    log.info("Part 3 : Processed: {} / {}", endIndex, videoIdsYoutube.size());
                }
                log.info("Part 3 : end - {}건의 videoIds로 detail한 정보 취득", videoDetail500.size());

                // 2. 500건의 Video 객체를 대상으로 루프.
                List<ArchiveEntity> finalEntities = new ArrayList<>();
                for (Video video : videoDetail500) {
                    // 2만건짜리 Map 에서 videoId로 value... 즉 DB에서 꺼내온 ArchiveEntity를 취득
                    ArchiveEntity tempEntity = updateMap.get(video.getId());

                    if (tempEntity != null) { // 값이 있음 -> 중복임 -> UPDATE 대상임 -> DB데이터를 가공함.
                        boolean isChanged = !tempEntity.getTitle().equals(video.getSnippet().getTitle())
                                || !tempEntity.getThumbnail().equals(getThumbnail(video))
                                || !tempEntity.getStartAt().equals(getStartAt(video));
                        if (isChanged) {
                            tempEntity.setTitle(video.getSnippet().getTitle());
                            tempEntity.setThumbnail(getThumbnail(video));
                            tempEntity.setStartAt(getStartAt(video));
                            updateCount++;
                            finalEntities.add(tempEntity);
                        }
                    } else { // 값이 없음 -> 신규임 -> Insert 대상임 -> 신입 Video 객체를 ArchiveEntity 객체로 갈아입혀야함.
                        finalEntities.add(convertVideoToArchiveEntity(video, channelIdAndMemberMap));
                        insertCount++;
                    }
                }

                // 3. 가공된 500건 이하의 List<ArchiveEntity> finalEntities 를 upsert
                log.info("Part 4 : start - {}건의 archive를 upsert 처리", finalEntities.size());
                if (!finalEntities.isEmpty()) {
                    archiveRepo.saveAll(finalEntities);
                    archiveRepo.flush();
                }
                log.info("Part 4 : end - {}건의 archive를 upsert 처리", finalEntities.size());
            }
            log.info("Part 4 : {}건의 행을 INSERT 하였습니다.", insertCount);
            log.info("Part 4 : {}건의 행을 UPDATE 하였습니다.", updateCount);
            //////////////////////////////////////////////////////////////
            log.info("{} - End insert all archive", currentDateTime.getCurrentDateTime());
            log.info("===================== Log end : insert all archive");
        } catch (Exception e) {
            log.error("치명적인 (혹은 예상못한) 에러 발생 : YoutubeContentService 클래스의 insertArchive 메서드에서 발생. " +
                    "어디서 터졌는지는 자세한 로그를 보세요. {}", e.getMessage(), e);
            throw e;
        }
    }

    // 유틸리티

    // insertArchive의 부품 1번쨰.
    private ArchiveEntity convertVideoToArchiveEntity(Video video, Map<String, MemberEntity> channelIdAndMemberMap) {
        return ArchiveEntity.builder()
                .member(channelIdAndMemberMap.get(video.getSnippet().getChannelId()))
                .videoId(video.getId())
                .title(video.getSnippet().getTitle())
                .thumbnail(getThumbnail(video))
                .startAt(getStartAt(video))
                .build();
    }

    // insertArchive의 부품 2번쨰.
    private String getThumbnail(Video video) {
        // 섬네일 단계별 추출
        // 제일 낮은 Default로 시작 -> High가 있어? 갈아끼워 -> Standard가 있어? 갈아끼워
        // Maxres가 있어? 갈아끼워. (끝)
        ThumbnailDetails thumbnails = video.getSnippet().getThumbnails();
        if (thumbnails == null) return null;
        String thumbnailUrl = thumbnails.getDefault().getUrl();
        if (thumbnails.getHigh() != null) thumbnailUrl = thumbnails.getHigh().getUrl();
        if (thumbnails.getStandard() != null) thumbnailUrl = thumbnails.getStandard().getUrl();
        if (thumbnails.getMaxres() != null) thumbnailUrl = thumbnails.getMaxres().getUrl();
        return thumbnailUrl;
    }

    // insertArchive의 부품 3번쨰.
    private OffsetDateTime getStartAt(Video video) {
        // 친절하게 단계별 null 체크를 통해 안전한 데이터를 뽑아보도록 합시다.
        // 옵셔널의 map은 여러개일때, null이 한번이라도 터지면 다른 map을 싹다 무시하고 or로 넘어가는 특성이 있기때문에
        // 1번 map의 결과가 null 이라고해서 2번 map에서 NPE가 터질일이 없어요.
        return Optional.ofNullable(video.getLiveStreamingDetails())
                .map(VideoLiveStreamingDetails::getActualStartTime)
                .map(googleDt -> OffsetDateTime.parse(googleDt.toStringRfc3339()))
                .orElseGet(() -> OffsetDateTime.parse(video.getSnippet().getPublishedAt().toStringRfc3339()));
        // ActualStartTime이 존재하면 그 값을, null이면 PublishedAt을 리턴합니다.
    }

    // 레코드 : insertLiveStream
    private record liveInsertDTO(String channelId, String videoId, String title, String liveStatus) {}
}
