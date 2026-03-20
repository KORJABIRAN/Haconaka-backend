package com.haconaka.demo.repository.archive;

import com.haconaka.demo.dto.archive.ArchiveSearchCondition;
import com.haconaka.demo.entity.ArchiveEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArchiveRepoCustom {

    List<ArchiveEntity> findAllByCondition(ArchiveSearchCondition condition, Pageable pageable);
    long countArchives(ArchiveSearchCondition condition);

}