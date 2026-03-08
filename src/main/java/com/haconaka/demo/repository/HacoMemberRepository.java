package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HacoMemberRepository extends JpaRepository<HacoMemberEntity, Integer> {
}
