package com.haconaka.demo.repository.livestream;

import com.haconaka.demo.entity.HacoCurrentLivestreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HacoCurrentLivestreamRepository
        extends JpaRepository<HacoCurrentLivestreamEntity, Integer>, HacoCurrentLivestreamRepositoryCustom {
    HacoCurrentLivestreamEntity findByAddress(String address);
}