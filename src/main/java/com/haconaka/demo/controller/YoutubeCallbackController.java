package com.haconaka.demo.controller;

import com.haconaka.demo.config.RestartConfig;
import com.haconaka.demo.service.youtube.YoutubeContentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class YoutubeCallbackController {

    private final YoutubeContentService youtubePubSubService;
    private final RestartConfig restartConfig;

    @GetMapping("/callback")
    public String verifySubscription(HttpServletRequest request) {
        String challenge = request.getParameter("hub.challenge");
        log.info("VERIFY callback: {}", challenge);
        return challenge != null ? challenge : "";
    }

    @PostMapping("/callback")
    public void handleNotification(@RequestBody String body) {
        log.info("Controller handleNotification called");
        restartConfig.restartSchedule();
        youtubePubSubService.handleNotification(body);
    }
}
