package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoCurrentLivestreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HacoCurrentLivestreamRepository
        extends JpaRepository<HacoCurrentLivestreamEntity, Integer> {
    List<HacoCurrentLivestreamEntity> findByAddress(String address);
}
