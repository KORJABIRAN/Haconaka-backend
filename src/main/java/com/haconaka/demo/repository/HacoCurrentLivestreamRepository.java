package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoCurrentLivestreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HacoCurrentLivestreamRepository
        extends JpaRepository<HacoCurrentLivestreamEntity, Integer> {
    HacoCurrentLivestreamEntity findByAddress(String address);
}
