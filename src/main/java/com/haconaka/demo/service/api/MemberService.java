package com.haconaka.demo.service.api;

import com.haconaka.demo.entity.MemberEntity;
import com.haconaka.demo.repository.member.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepo MemberRepo;

    public List<MemberEntity> selectAllMembers() {
        return MemberRepo.findAll();
    }

}
