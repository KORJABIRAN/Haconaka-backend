package com.haconaka.demo.repository.member;

import com.haconaka.demo.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepo extends JpaRepository<MemberEntity, Long> {
    MemberEntity findByYoutubeChannelId(String channelId);
}
