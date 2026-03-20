package com.haconaka.demo.dto.archive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveDetailDTO {

    // ID
    private Long id;

    // 영상 ID
    private String videoId;

    // 방송 제목
    private String title;

    // 시작 시간
    private OffsetDateTime startAt;

    // 카테고리
    private String category;

    // 합방 유형
    private String collaboType;

    // 멤버 이름
    private String name;

    // 프로필 아이콘
    private String icon;

    // 퍼스널 컬러
    private String personalColor;

    // 유튜브 채널ID
    private String youtubeChannelId;

    // 트위터 URL
    private String twitterUrl;

    // 합방 멤버 목록
    private List<ArchiveDetailInnerDTO> collaboMembers;
}