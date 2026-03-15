package com.haconaka.demo.service.api;

import com.haconaka.demo.dto.archive.ArchiveItemDTO;
import com.haconaka.demo.repository.archive.ArchiveRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepo archiveRepo;

    public Page<ArchiveItemDTO> selectAllArchive(int page) {

        return archiveRepo.findAll(PageRequest.of(0, page, Sort.by("startAt").descending()))
                .map( c -> ArchiveItemDTO.builder()
                        .id(c.getId())
                        .thumbnail(c.getThumbnail())
                        .videoId(c.getVideoId())
                        .name(c.getMember().getName())
                        .title(c.getTitle())
                        .icon(c.getMember().getIcon())
                        .startAt(c.getStartAt())
                        .build());
    }

}
