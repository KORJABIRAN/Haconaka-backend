package com.haconaka.demo.repository;

import com.haconaka.demo.entity.HacoCurrentLivestream;
//import com.haconaka.demo.entity.HacoCurrentLivestreamTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HacoCurrentLivestreamRepository
        extends JpaRepository<HacoCurrentLivestream, Integer> {

//    List<HacoCurrentLivestream> findByMemberPk(Integer memberPk);
    List<HacoCurrentLivestream> findByAddress(String address);
//    List<HacoCurrentLivestream> deleteByAddress(String address);


}
