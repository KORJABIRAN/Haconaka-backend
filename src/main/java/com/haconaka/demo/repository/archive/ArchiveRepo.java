package com.haconaka.demo.repository.archive;

import com.haconaka.demo.entity.ArchiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchiveRepo extends JpaRepository<ArchiveEntity, Integer>, ArchiveRepoCustom {

    @Query("SELECT a.videoId FROM ArchiveEntity a")
    List<String> findAllVideoIds();
}