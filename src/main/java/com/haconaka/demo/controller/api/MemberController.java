package com.haconaka.demo.controller.api;

import com.haconaka.demo.dto.member.MemberDropdownDTO;
import com.haconaka.demo.service.api.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/dropdown")
    public ResponseEntity<Map<Short, List<MemberDropdownDTO>>> getMembers() {
        return ResponseEntity.ok().body(memberService.selectAllMembersForDropdown());
    }
}
