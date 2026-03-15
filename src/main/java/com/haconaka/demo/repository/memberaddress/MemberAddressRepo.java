package com.haconaka.demo.repository.memberaddress;

import com.haconaka.demo.entity.MemberAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAddressRepo extends JpaRepository<MemberAddressEntity, Integer> {
}