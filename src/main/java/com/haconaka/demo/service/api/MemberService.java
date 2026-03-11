package com.haconaka.demo.service.api;

import com.haconaka.demo.entity.HacoMemberEntity;
import com.haconaka.demo.repository.HacoMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final HacoMemberRepository hacoMemberRepository;

    public List<HacoMemberEntity> selectAllMembers() {
        return hacoMemberRepository.findAll();
    }

}
