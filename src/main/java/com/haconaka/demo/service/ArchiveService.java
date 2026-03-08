package com.haconaka.demo.service;

import com.haconaka.demo.entity.HacoArchiveEntity;
import com.haconaka.demo.entity.HacoCurrentLivestreamEntity;
import com.haconaka.demo.repository.HacoArchiveRepository;
import com.haconaka.demo.repository.HacoCurrentLivestreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final HacoArchiveRepository hacoArchiveRepository;

    public Page<HacoArchiveEntity> selectAllArchive() {
        return hacoArchiveRepository.findAll(Pageable.ofSize(10));
    }

}
