package com.haconaka.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PubSubNotificationDto {
    private String channelId;
    private String videoId;
    private String title;
    private String publishedAt;
}
