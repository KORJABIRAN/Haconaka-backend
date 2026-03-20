package com.haconaka.demo.repository.archive;

import com.haconaka.demo.dto.archive.ArchiveSearchCondition;
import com.haconaka.demo.entity.ArchiveEntity;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.haconaka.demo.entity.QArchiveEntity.archiveEntity;

@RequiredArgsConstructor
public class ArchiveRepoImpl implements ArchiveRepoCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ArchiveEntity> findAllByCondition(ArchiveSearchCondition condition, Pageable pageable) {
        return queryFactory
                .selectFrom(archiveEntity)
                .where(
                        search(condition.getSearch()),
                        category(condition.getCategory()),
                        member(condition.getMember())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .fetch();
    }

    @Override
    public long countArchives(ArchiveSearchCondition condition) {
        Long count = queryFactory
                .select(archiveEntity.count())
                .from(archiveEntity)
                .where(
                        search(condition.getSearch()),
                        category(condition.getCategory()),
                        member(condition.getMember())
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    // 제목 검색 조건 (현재 filter="title" 고정이라 가정)
    private BooleanExpression search(String search) {
        if (search == null || search.isBlank()) return null;
        return archiveEntity.title.containsIgnoreCase(search);
    }

    // 카테고리 다중 선택 조건 (List<String>)
    private BooleanExpression category(List<String> categories) {
        if (categories == null || categories.isEmpty()) return null;
        return archiveEntity.category.in(categories);
    }

    // 멤버 다중 선택 조건 (List<String>)
    private BooleanExpression member(List<String> members) {
        if (members == null || members.isEmpty()) return null;
        return archiveEntity.member.nameEn.in(members);
    }

    // 정렬 유틸리티
    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    PathBuilder<ArchiveEntity> path = new PathBuilder<>(ArchiveEntity.class, "archiveEntity");
                    return new OrderSpecifier(direction, path.get(order.getProperty()));
                })
                .toArray(OrderSpecifier[]::new);
    }
}