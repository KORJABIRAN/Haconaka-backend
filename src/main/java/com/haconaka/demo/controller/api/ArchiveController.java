package com.haconaka.demo.controller.api;

import com.haconaka.demo.dto.archive.ArchiveDetailDTO;
import com.haconaka.demo.dto.archive.ArchiveItemDTO;
import com.haconaka.demo.dto.archive.ArchiveSearchCondition;
import com.haconaka.demo.service.api.ArchiveService;
import com.haconaka.demo.service.youtube.YoutubeContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final YoutubeContentService youtubeContentService;

    @GetMapping("/archives")
    public ResponseEntity<Page<ArchiveItemDTO>> getAllArchives(
            @ModelAttribute
            ArchiveSearchCondition condition,
            @PageableDefault(
                    size = 20,
                    sort = "startAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(archiveService.selectAllArchivesWithCondition(condition, pageable));
    }

    @GetMapping("/archive/{videoId}")
    public ResponseEntity<ArchiveDetailDTO> getArchive(@PathVariable("videoId") String videoId) {
        return ResponseEntity.ok().body(archiveService.selectOneArchive(videoId));
    }

    // All Archive Insert (약 2만건)
    @PostMapping("/archives/archiveInsert")
    public ResponseEntity<?> insertAllArchive(@RequestParam(name = "isAll", defaultValue = "true") boolean isAll ) {
        // true : AllArchive / false : 각 채널마다 50건 / 기본값은 ture 입니다.
        youtubeContentService.insertArchive(isAll);
        return ResponseEntity.ok().build();
    }
}
