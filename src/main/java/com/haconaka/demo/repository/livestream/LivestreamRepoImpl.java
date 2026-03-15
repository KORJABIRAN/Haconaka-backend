package com.haconaka.demo.repository.livestream;

import com.haconaka.demo.dto.livestream.LiveStreamItemDTO;

import java.util.List;


public class LivestreamRepoImpl implements LivestreamRepoCustom {

    @Override
    public List<LiveStreamItemDTO> findAllLiveStream() {
        return List.of();
    }
}