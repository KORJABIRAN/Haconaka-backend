package com.haconaka.demo.service;

import com.haconaka.demo.entity.HacoAddress;
import com.haconaka.demo.repository.HacoAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeSubscriptionService {

    private final HacoAddressRepository addressRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.callback-base-url}")
    private String callbackBaseUrl;

    private static final String HUB_URL = "https://pubsubhubbub.appspot.com/subscribe";

    public void subscribeAllYtChannels() {
        List<HacoAddress> channels = addressRepository.findByCategory("YchannelID");
        log.info("Found {} YchannelID rows", channels.size());

        for (HacoAddress addr : channels) {
            String channelId = addr.getAddress();
            if (channelId == null || channelId.isBlank()) {
                continue;
            }
            try {
                subscribeChannel(channelId);
            } catch (Exception e) {
                log.error("Failed to subscribe channelId={} (a_pk={})",
                        channelId, addr.getId(), e);
            }
        }
    }

    private void subscribeChannel(String channelId) {
        String topic = "https://www.youtube.com/xml/feeds/videos.xml?channel_id="
                + URLEncoder.encode(channelId, StandardCharsets.UTF_8);
        String callback = callbackBaseUrl + "/youtube/callback";

        String body = "hub.mode=subscribe"
                + "&hub.topic=" + topic
                + "&hub.callback=" + URLEncoder.encode(callback, StandardCharsets.UTF_8)
                + "&hub.verify=async"
                + "&hub.verify_token=testtoken";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp =
                restTemplate.exchange(HUB_URL, HttpMethod.POST, entity, String.class);

        log.info("Subscribe channelId={} status={}", channelId, resp.getStatusCode());
    }
}
