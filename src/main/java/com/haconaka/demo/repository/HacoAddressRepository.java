package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface HacoAddressRepository extends JpaRepository<HacoAddress, Integer> {
    List<HacoAddress> findByAddress(String address);
    List<HacoAddress> findByCategory(String category);
}