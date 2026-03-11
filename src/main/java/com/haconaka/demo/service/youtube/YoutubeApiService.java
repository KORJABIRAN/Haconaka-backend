package com.haconaka.demo.service.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeApiService {

    private final YouTube youtube;
    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    // Youtube API : videoId로 video 리스트 취득
    public List<Video> getYoutubeStatusByVideoId(List<String> videoIds) {
        try {
            // YOUTUBE API 호출 과정
            YouTube.Videos.List req = youtube.videos().list(List.of("snippet", "status", "liveStreamingDetails"));
            req.setId(videoIds);
            req.setKey(youtubeApiKey);
            VideoListResponse resp = req.execute();
            return resp.getItems();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(new Video());
        }
    }
}
