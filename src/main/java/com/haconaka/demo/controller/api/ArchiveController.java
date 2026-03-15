package com.haconaka.demo.controller.api;

import com.haconaka.demo.dto.archive.ArchiveItemDTO;
import com.haconaka.demo.service.api.ArchiveService;
import com.haconaka.demo.service.youtube.YoutubeContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final YoutubeContentService youtubeContentService;

    @GetMapping("/archives")
    public ResponseEntity<Page<ArchiveItemDTO>> getAllArchive(
            @RequestParam(value = "page", defaultValue = "10") int page
    ) {
        return ResponseEntity.ok().body(archiveService.selectAllArchive(page));
    }

    // All Archive Insert (약 2만건)
    @PostMapping("/archives/AllArchiveInsert")
    public ResponseEntity<?> insertTestArchive() {
        // true : AllArchive / false : 각 채널마다 50건 / 기본값은 ture 입니다.
        youtubeContentService.insertAllArchive(true);
        return ResponseEntity.ok().build();
    }
}
