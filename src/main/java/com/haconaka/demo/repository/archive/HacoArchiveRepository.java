package com.haconaka.demo.repository.archive;

import com.haconaka.demo.entity.HacoArchiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HacoArchiveRepository
        extends JpaRepository<HacoArchiveEntity, Integer>, HacoArchiveRepositoryCustom {

}
