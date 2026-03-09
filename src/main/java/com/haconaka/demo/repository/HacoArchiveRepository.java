package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoArchiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HacoArchiveRepository extends JpaRepository<HacoArchiveEntity, Integer> {
}
