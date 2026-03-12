package com.haconaka.demo.service.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeApiService {

    private final YouTube youtube;
    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    // Youtube API : videoId로 video 리스트 취득
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
}
