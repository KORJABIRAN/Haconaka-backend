package com.haconaka.demo.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepoImpl implements MemberRepoCustom {

    private final JPAQueryFactory queryFactory;

}