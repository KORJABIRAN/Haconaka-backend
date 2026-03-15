package com.haconaka.demo.service.api;

import com.haconaka.demo.dto.livestream.LiveStreamItemDTO;
import com.haconaka.demo.repository.livestream.LivestreamRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveStreamService {

    private final LivestreamRepo liveStreamRepo;

    public List<LiveStreamItemDTO> selectAllLiveStream() {

        return liveStreamRepo.findAll().stream()
                .map(l -> LiveStreamItemDTO.builder()
                        .id(l.getId())
                        .memberId(l.getMember().getId())
                        .name(l.getMember().getName())
                        .icon(l.getMember().getIcon())
                        .videoId(l.getVideoId())
                        .build()
                ).toList();
    }
}
