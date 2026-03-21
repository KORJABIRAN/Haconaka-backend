package com.haconaka.demo.service.youtube;

import com.google.api.services.youtube.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    // 이름만 그럴듯한 껍데기! (LiveStream INSERT가 들어있어요)
    public void handleNotification(String atomXml) {
        insertLiveStream(atomXml);
    }

    // LiveStream INSERT 로직
    public void insertLiveStream(String atomXml) {
        try {
            // TODO:데이터가1건이 아닌경우에 대해 예외처리를 할 필요가 있음.
            // 여기서는 pubSub으로 들어온 요청이 무조건 1개라고 가정합니다.
            PubSubNotificationDto pubSubData = xmlParsingService.parseAtomXml(atomXml).get(0);
            String channelId = pubSubData.getChannelId();
            String videoId = pubSubData.getVideoId();
            String title = pubSubData.getTitle();
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
                    .title(title)
                    .build());
            log.info("{} - succeed save.", currentDateTime.getCurrentDateTime());
        } catch (Exception e) {
            log.error("Exception : Failed to handle notification");
            e.getStackTrace();
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

            // Youtube Data API : 재생목록 내 전체 동영상 취득
            log.info("Part 1 : start - playlistIds로 PlaylistItem 취득");
            List<PlaylistItem> allPlaylistItems = youtubeApi.getYoutubeVideosInPlaylist(playlistIds, isAll);
            log.info("Part 1 : end - playlistIds로 PlaylistItem 취득. 총 비디오 개수: {}", allPlaylistItems.size());
            List<String> videoIdsYoutube = allPlaylistItems.stream()
                    .map(data -> data.getContentDetails().getVideoId()).toList();

            // Youtube Data API : 취득한 전체동영상의 videoIds 로 DetailList를 조회
            List<Video> allVideoDetail = new ArrayList<>();
            log.info("Part 2 : start - {}건의 videoIds로 detail한 정보 취득", allPlaylistItems.size());
            for (int i = 0; i < videoIdsYoutube.size(); i += 50) {
                int endIndex = Math.min(i + 50, videoIdsYoutube.size());
                List<String> videoIdsSize50 = videoIdsYoutube.subList(i, endIndex);
                allVideoDetail.addAll(youtubeApi.getYoutubeStatusByVideoId(videoIdsSize50));
                log.info("Part 2 : Processed: {} / {}", endIndex, videoIdsYoutube.size());
            }
            log.info("Part 2 : end - {}건의 videoIds로 detail한 정보 취득", allPlaylistItems.size());

            // 1만건의 videoIds 추출
            // 이미 했네? videoIdsYoutube 가 있네?

            // 1만건의 videoIds로 중복인 DB 데이터를 긁어옴. 500건씩 끊어서 조회함.
            log.info("Part 3 : start - {}건의 videoIds로 중복값을 DB에서 조회", videoIdsYoutube.size());
            List<ArchiveEntity> updateArchiveEntities = new ArrayList<>();
            for (int i = 0; i < videoIdsYoutube.size(); i += 500) {
                int endIndex = Math.min(i + 500, videoIdsYoutube.size());
                List<String> videoIdsSize500 = videoIdsYoutube.subList(i, endIndex);
                updateArchiveEntities.addAll(archiveRepo.findAllByVideoIdIn(videoIdsSize500));
                log.info("Part 3 : Processed: {} / {}", endIndex, videoIdsYoutube.size());
            }
            log.info("Part 3 : end - {}건의 videoIds로 중복값을 DB에서 조회. 총 중복 개수 : {}",
                    videoIdsYoutube.size(),  updateArchiveEntities.size());

            // 검색이 쉽도록 위 리스트를 Map으로 바꿀거임.
            Map<String, ArchiveEntity> updateMap = updateArchiveEntities.stream()
                    .collect(Collectors.toMap(ArchiveEntity::getVideoId, a -> a));

            // 1만건의 Video 객체를 대상으로 루프.
            int insertCount = 0;
            int updateCount = 0;
            List<ArchiveEntity> finalEntities = new ArrayList<>();
            for (Video video : allVideoDetail) {
                // Map 에서 videoId로 value를 (DB에서 꺼내온 ArchiveEntity를) 취득
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

            // 가공이 길었다. 드디어 saveAll을 할 시간임. 근데 얘도 끊어서 돌려야됨. 500건씩 끊어볼까?
            log.info("Part 4 : {}건의 archive를 upsert 처리 시작", finalEntities.size());
            if (!finalEntities.isEmpty()) {
                for (int i = 0; i < finalEntities.size(); i += 500) {
                    int endIndex = Math.min(i + 500, finalEntities.size());
                    List<ArchiveEntity> entities500 = finalEntities.subList(i, endIndex);
                    archiveRepo.saveAll(entities500);
                    archiveRepo.flush();
                    log.info("Part 4 : Processed: {} / {}", endIndex, finalEntities.size());
                }
                log.info("Part 4 : {}건의 archive를 upsert 처리 완료", finalEntities.size());
                log.info("Part 4 : {}건의 행을 INSERT 하였습니다.", insertCount);
                log.info("Part 4 : {}건의 행을 UPDATE 하였습니다.", updateCount);
            }
            log.info("{} - End insert all archive", currentDateTime.getCurrentDateTime());
            log.info("===================== Log end : insert all archive");
        } catch (Exception e) {
            log.error("치명적인 (혹은 예상못한) 에러 발생 : YoutubeContentService 클래스의 insertArchive 메서드에서 발생. " +
                    "어디서 터졌는지는 자세한 로그를 보세요. {}", e.getMessage(), e);
            throw e;
        }
    }

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
}
