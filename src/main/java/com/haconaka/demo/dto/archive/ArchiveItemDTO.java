package com.haconaka.demo.dto.archive;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@Builder
public class ArchiveItemDTO {

    // 고유식별값(PK)
    private Long id;

    // 섬네일
    private String thumbnail;

    // 비디오주소
    private String videoId;

    // 제목
    private String title;

    // 날짜+시간
    private OffsetDateTime startAt;

    // 아이콘
    private String icon;

    // 멤버 이름
    private String name;

    @QueryProjection
    public ArchiveItemDTO(
            Long id, String thumbnail, String videoId, String title, OffsetDateTime startAt, String icon, String name) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.videoId = videoId;
        this.title = title;
        this.startAt = startAt;
        this.icon = icon;
        this.name = name;
    }
}
