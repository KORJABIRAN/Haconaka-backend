package com.haconaka.demo.dto.archive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveDetailInnerDTO {

    // 멤버 이름
    private String name;

    // 프로필 아이콘
    private String icon;

    // 유튜브 채널ID
    private String youtubeChannelId;

    // 트위터 URL
    private String twitterUrl;

}