package com.haconaka.demo.controller;

import com.google.api.services.youtube.model.LiveStream;
import com.haconaka.demo.service.LiveStreamService;
import com.haconaka.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LiveStreamController {

    private final LiveStreamService liveStreamService;

    @GetMapping("/liveStream")
    public ResponseEntity<?> getLiveStream() {
        return ResponseEntity.ok().body(liveStreamService.selectAllLiveStream());
    }
}
