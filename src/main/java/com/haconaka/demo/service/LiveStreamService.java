package com.haconaka.demo.service;

import com.haconaka.demo.entity.HacoCurrentLivestreamEntity;
import com.haconaka.demo.repository.HacoCurrentLivestreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveStreamService {

    private final HacoCurrentLivestreamRepository hacoCurrentLivestreamRepository;

    public List<HacoCurrentLivestreamEntity> selectAllLiveStream() {
        return hacoCurrentLivestreamRepository.findAll();
    }

}
