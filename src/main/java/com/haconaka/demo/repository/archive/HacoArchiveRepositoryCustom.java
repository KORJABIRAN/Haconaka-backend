package com.haconaka.demo.repository.archive;

import com.haconaka.demo.dto.ArchiveItemDTO;

import java.util.List;

public interface HacoArchiveRepositoryCustom {
    List<ArchiveItemDTO> findAllArchive();
}