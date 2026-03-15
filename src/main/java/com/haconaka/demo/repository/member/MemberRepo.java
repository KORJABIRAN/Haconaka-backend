package com.haconaka.demo.repository.member;

import com.haconaka.demo.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepo extends JpaRepository<MemberEntity, Integer> {
    MemberEntity findByYoutubeChannelId(String channelId);
}
