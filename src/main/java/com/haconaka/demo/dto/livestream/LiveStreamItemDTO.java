package com.haconaka.demo.dto.livestream;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class LiveStreamItemDTO {
    private Long id;
    private Long memberId;
    private String name;
    private String title;
    private String icon;
    private String videoId;
    private String channelId;

    @QueryProjection
    public LiveStreamItemDTO(Long id, Long memberId, String name, String title, String icon, String videoId, String channelId) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.title = title;
        this.icon = icon;
        this.videoId = videoId;
        this.channelId = channelId;
    }

}
