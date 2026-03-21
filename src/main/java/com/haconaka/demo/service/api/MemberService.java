package com.haconaka.demo.service.api;

import com.haconaka.demo.dto.member.MemberDropdownDTO;
import com.haconaka.demo.entity.MemberEntity;
import com.haconaka.demo.repository.member.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepo memberRepo;

    public Map<Short, List<MemberDropdownDTO>> selectAllMembersForDropdown() {
        return memberRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        MemberEntity::getGeneration, // 1. 기수별로 묶어라 (Key)
                        Collectors.mapping(data -> MemberDropdownDTO.builder() // 2. 각 멤버를 DTO로 변환해라
                                        .id(data.getId())
                                        .icon(data.getIcon())
                                        .nameEn(data.getNameEn())
                                        .name(data.getName())
                                        .build(),
                                Collectors.toList()) // 3. 변환된 DTO들을 리스트로 담아라 (Value)
                ));
    }
}
