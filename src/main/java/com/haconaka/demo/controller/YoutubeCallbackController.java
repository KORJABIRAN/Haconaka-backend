package com.haconaka.demo.controller;

import com.haconaka.demo.service.YoutubePubSubService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class YoutubeCallbackController {

    private final YoutubePubSubService youtubePubSubService;

    @GetMapping("/callback")
    public String verifySubscription(HttpServletRequest request) {
        String challenge = request.getParameter("hub.challenge");
        log.info("VERIFY callback: {}", challenge);
        return challenge != null ? challenge : "";
    }

    @PostMapping("/callback")
    public void handleNotification(@RequestBody String body) {
        log.info("Controller handleNotification called");
        youtubePubSubService.handleNotification(body);
    }
}
