package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface HacoAddressRepository extends JpaRepository<HacoAddressEntity, Integer> {
    HacoAddressEntity findByAddress(String address);
    List<HacoAddressEntity> findByCategory(String category);
}