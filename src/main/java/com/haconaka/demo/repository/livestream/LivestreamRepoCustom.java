package com.haconaka.demo.repository.livestream;

import com.haconaka.demo.dto.livestream.LiveStreamItemDTO;
import java.util.List;

public interface LivestreamRepoCustom {
    List<LiveStreamItemDTO> findAllLiveStream();
}