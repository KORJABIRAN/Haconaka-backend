package com.haconaka.demo.service.youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public List<PlaylistItem> getYoutubeVideosInPlaylist(List<String> playlistIds, boolean isAll) {
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
            }
        }
        return allItems;
    }
}
