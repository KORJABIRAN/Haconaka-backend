package com.haconaka.demo.repository.livestream;

import com.haconaka.demo.entity.LiveStreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivestreamRepo
        extends JpaRepository<LiveStreamEntity, Integer>, LivestreamRepoCustom {
    LiveStreamEntity findByVideoId(String videoId);
}