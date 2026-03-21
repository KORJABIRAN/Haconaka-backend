package com.haconaka.demo.service.youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeApiService {

    private final YouTube youtube;
    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    // Youtube Data API : videoIds로 video 리스트 취득
    public List<Video> getYoutubeStatusByVideoId(List<String> videoIds) {
        try {
            // videoIds가 empty이면 400에러가 터져요. 조건문으로 검사 필수.
            if (videoIds.isEmpty()) return List.of();
            // YOUTUBE API 호출 과정
            YouTube.Videos.List req = youtube.videos().list(List.of("snippet", "status", "liveStreamingDetails"));
            req.setId(videoIds);
            req.setKey(youtubeApiKey);
            VideoListResponse resp = req.execute();
            return resp.getItems();
        } catch (IOException e) {
            log.warn("뭔가 예상치 못한 Exception 입니다 : YoutubeApiService 클래스 getYoutubeStatusByVideoId 에서 catch가 호출됨. ");
            e.printStackTrace();
            return List.of();
        }
    }

    // Youtube Data API : playlistId로 재생 목록 내 전체 video 리스트 취득
    public List<PlaylistItem> getYoutubePlaylistItemsInPlaylist(List<String> playlistIds, boolean isAll) {
        List<PlaylistItem> allItems = new ArrayList<>();
        for (String playlistId : playlistIds) {
            String nextPageToken = null; // 주의: 루프 시작 시 토큰 초기화 필수!

            try {
                do {
                    YouTube.PlaylistItems.List request = youtube.playlistItems()
                            .list(List.of("snippet", "contentDetails"));
                    request.setKey(youtubeApiKey);
                    request.setPlaylistId(playlistId);
                    request.setMaxResults(50L);
                    request.setPageToken(nextPageToken);
                    PlaylistItemListResponse response = request.execute();

                    List<PlaylistItem> items = response.getItems();

                    if (items != null) {
                        allItems.addAll(items);
                        log.info("Processed: total {}", allItems.size());
                    }

                    // 여기 중요함. 브레이크 없으면 2만건 전체조회임. 브레이크가 있어야 각 채널마다 50건 조회임.
                    if (!isAll) break;

                    nextPageToken = response.getNextPageToken();

                } while (nextPageToken != null);

                log.info("Successfully fetched playlist: {}", playlistId);

            } catch (GoogleJsonResponseException e) {
                log.warn("🚨 이 ID는 막힌 ID입니다. : {} (사유: {}, 코드: {})",
                        playlistId, e.getDetails().getMessage(), e.getStatusCode());
            } catch (IOException e) {
                log.error("Network or General error for playlistId: {}", playlistId, e);
            } catch (Exception e) {
                log.warn("뭔가 예상치 못한 Exception 입니다 : YoutubeApiService 클래스 getYoutubeVideosInPlaylist 에서 catch가 호출됨. ");
                e.printStackTrace();
            }
        }
        return allItems;
    }

    // 긴급!!! Youtube Data API : playlistId로 재생 목록 내 전체 >> String videoId << 만을 취득
    // All Archive 조회를 하면 1만건 이상의 무거운 데이터가 메모리에 쌓여 터집니다.
    // 그래서, 하나의 전용 메소드를 추가하여 videoIds 만을 반환하도록 하였습니다.
    public List<String> getYoutubeVideoIdsInPlaylist(List<String> playlistIds, boolean isAll) {
        List<String> allVideoIds = new ArrayList<>();
        for (String playlistId : playlistIds) {
            String nextPageToken = null; // 주의: 루프 시작 시 토큰 초기화 필수!

            try {
                do {
                    YouTube.PlaylistItems.List request = youtube.playlistItems()
                            .list(List.of("contentDetails"));
                    request.setKey(youtubeApiKey);
                    request.setPlaylistId(playlistId);
                    request.setMaxResults(50L);
                    request.setPageToken(nextPageToken);
                    PlaylistItemListResponse response = request.execute();

                    List<String> tempVideoIds50 = Optional.ofNullable(response.getItems()) // 1. items 자체가 null인지 체크
                            .orElse(Collections.emptyList()) // null이면 빈 리스트로 대체
                            .stream()
                            .filter(Objects::nonNull) // 2. item 객체 자체가 null인 경우 필터링
                            .map(PlaylistItem::getContentDetails)
                            .filter(Objects::nonNull) // 3. contentDetails가 null인 경우 필터링 (NPE 방지 핵심!)
                            .map(PlaylistItemContentDetails::getVideoId)
                            .filter(StringUtils::hasText) // 4. videoId가 비어있거나 null인 경우 제외 (Spring StringUtils 사용 시)
                            .toList();

                    allVideoIds.addAll(tempVideoIds50);
                    log.info("Part 1 : Processed: total {}", allVideoIds.size());

                    // 여기 중요함. 브레이크 없으면 2만건 전체조회임. 브레이크가 있어야 각 채널마다 50건 조회임.
                    if (!isAll) break;

                    nextPageToken = response.getNextPageToken();

                } while (nextPageToken != null);

            } catch (GoogleJsonResponseException e) {
                log.warn("🚨 이 ID는 막힌 ID입니다. : {} (사유: {}, 코드: {})",
                        playlistId, e.getDetails().getMessage(), e.getStatusCode());
            } catch (IOException e) {
                log.error("Network or General error for playlistId: {}", playlistId, e);
            } catch (Exception e) {
                log.warn("뭔가 예상치 못한 Exception 입니다 : YoutubeApiService 클래스 getYoutubeVideoIdsInPlaylist 에서 catch가 호출됨. ");
                e.printStackTrace();
            }
        }
        return allVideoIds;
    }
}
