package com.haconaka.demo.repository.memberaddress;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberAddressRepoImpl implements MemberAddressRepoCustom {

    private final JPAQueryFactory queryFactory;

}