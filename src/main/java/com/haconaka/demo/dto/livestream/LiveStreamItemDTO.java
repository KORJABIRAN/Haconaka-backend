package com.haconaka.demo.dto.livestream;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class LiveStreamItemDTO {
    private int id;
    private int memberPk;
    private String name;
    private String icon;
    private String videoId;

    @QueryProjection
    public LiveStreamItemDTO(int id, int memberPk, String name, String icon, String videoId) {
        this.id = id;
        this.memberPk = memberPk;
        this.name = name;
        this.icon = icon;
        this.videoId = videoId;
    }

}
