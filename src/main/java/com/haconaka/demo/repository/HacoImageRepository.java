package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HacoImageRepository extends JpaRepository<HacoImage, Integer> {

}
