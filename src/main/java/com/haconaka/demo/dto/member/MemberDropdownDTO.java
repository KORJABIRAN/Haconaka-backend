package com.haconaka.demo.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDropdownDTO {
    private Long id;
    private String name;
    private String nameEn;
    private String icon;
}
