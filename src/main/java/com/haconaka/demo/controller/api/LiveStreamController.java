package com.haconaka.demo.controller.api;

import com.haconaka.demo.dto.livestream.LiveStreamItemDTO;
import com.haconaka.demo.service.api.LiveStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://haconaka-frontend.vercel.app")
public class LiveStreamController {

    private final LiveStreamService liveStreamService;

    @GetMapping("/liveStreams")
    public ResponseEntity<List<LiveStreamItemDTO>> getLiveStream() {
        return ResponseEntity.ok().body(liveStreamService.selectAllLiveStream());
    }
}