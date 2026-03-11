package com.haconaka.demo.controller.api;

import com.haconaka.demo.service.api.ArchiveService;
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
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping("/archive")
    public ResponseEntity<?> getAllArchive() {
        return ResponseEntity.ok().body(archiveService.selectAllArchive());
    }
}
