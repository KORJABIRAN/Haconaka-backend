package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoCurrentLivestreamTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HacoCurrentLivestreamTestRepository
        extends JpaRepository<HacoCurrentLivestreamTest, Integer> {

//    List<HacoCurrentLivestreamTest> findByMemberPk(Integer memberPk);
    List<HacoCurrentLivestreamTest> findByAddress(String address);
//    List<HacoCurrentLivestreamTest> deleteByAddress(String address);


}
