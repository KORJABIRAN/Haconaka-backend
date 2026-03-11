package com.haconaka.demo.service.api;

import com.haconaka.demo.entity.HacoArchiveEntity;
import com.haconaka.demo.repository.HacoArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final HacoArchiveRepository hacoArchiveRepository;

    public Page<HacoArchiveEntity> selectAllArchive() {
        return hacoArchiveRepository.findAll(Pageable.ofSize(10));
    }

}
