package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HacoMemberRepository extends JpaRepository<HacoMember, Integer> {
//    Optional findByYoutubeChannelId(String youtubeChannelId);
}
