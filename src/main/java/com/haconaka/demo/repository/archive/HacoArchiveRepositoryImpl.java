package com.haconaka.demo.repository.archive;

import com.haconaka.demo.dto.ArchiveItemDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class HacoArchiveRepositoryImpl implements HacoArchiveRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ArchiveItemDTO> findAllArchive() { // TODO:여기 입맛에맞게 수정
//        return queryFactory
//                .select(Projections.fields(LiveStreamItemDTO.class,
//                        hacoCurrentLivestreamEntity.id,
//                        hacoCurrentLivestreamEntity.memberPk,
//                        hacoCurrentLivestreamEntity.address.as("videoId"),
//                        hacoMemberEntity.name,
//                        hacoImage.icon
//                ))
//                .from(hacoCurrentLivestreamEntity)
//                .leftJoin(hacoMemberEntity).on(hacoCurrentLivestreamEntity.memberPk.eq(hacoMemberEntity.id))
//                .leftJoin(hacoImage).on(hacoCurrentLivestreamEntity.memberPk.eq(hacoImage.memberPk))
//                .fetch();
        return List.of();
    }
}
