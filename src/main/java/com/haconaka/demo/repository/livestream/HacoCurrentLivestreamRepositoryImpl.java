package com.haconaka.demo.repository.livestream;

import com.haconaka.demo.dto.livestream.LiveStreamItemDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.haconaka.demo.entity.QHacoCurrentLivestreamEntity.hacoCurrentLivestreamEntity;
import static com.haconaka.demo.entity.QHacoImage.hacoImage;
import static com.haconaka.demo.entity.QHacoMemberEntity.hacoMemberEntity;

@RequiredArgsConstructor
public class HacoCurrentLivestreamRepositoryImpl implements HacoCurrentLivestreamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LiveStreamItemDTO> findAllLiveStream() {
        return queryFactory
                .select(Projections.fields(LiveStreamItemDTO.class,
                        hacoCurrentLivestreamEntity.id,
                        hacoCurrentLivestreamEntity.memberPk,
                        hacoCurrentLivestreamEntity.address.as("videoId"),
                        hacoMemberEntity.name,
                        hacoImage.icon
                ))
                .from(hacoCurrentLivestreamEntity)
                .leftJoin(hacoMemberEntity).on(hacoCurrentLivestreamEntity.memberPk.eq(hacoMemberEntity.id))
                .leftJoin(hacoImage).on(hacoCurrentLivestreamEntity.memberPk.eq(hacoImage.memberPk))
                .fetch();
    }
}