package com.haconaka.demo.service.api;

import com.haconaka.demo.dto.livestream.LiveStreamItemDTO;
import com.haconaka.demo.repository.livestream.HacoCurrentLivestreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveStreamService {

    private final HacoCurrentLivestreamRepository hacoCurrentLivestreamRepository;

    public List<LiveStreamItemDTO> selectAllLiveStream() {
        return hacoCurrentLivestreamRepository.findAllLiveStream();
    }

}
