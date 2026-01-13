package com.haconaka.demo.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestGetSubscription {

    private final YouTube youTube;
    @Value("${youtube.api-key}")
    private String youtubeApiKey;

    public void getSubscription() throws IOException {
        YouTube.Subscriptions.List req = youTube.subscriptions().list(List.of("snippet"));
        req.setMine(true);
        req.setKey(youtubeApiKey);
        SubscriptionListResponse response = req.execute();
        System.out.println(response.getItems().size());
    }

}
