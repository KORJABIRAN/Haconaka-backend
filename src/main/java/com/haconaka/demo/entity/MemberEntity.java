package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member", indexes = {
        @Index(name = "idx_member_name_en", columnList = "name_en", unique = true)
})
public class MemberEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "name_en", nullable = false, length = 512, columnDefinition = "VARCHAR(64) DEFAULT 'DEFAULT'")
    @Builder.Default
    private String nameEn = "DEFAULT";

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "debut_date", nullable = false)
    private LocalDate debutDate;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(nullable = false)
    private Short generation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String introduce;

    @Column(name = "mother_name", nullable = false, length = 64)
    private String motherName;

    @Column(name = "mother_twitter", nullable = false, length = 512)
    private String motherTwitter;

    @Column(name = "personal_color", nullable = false, length = 16)
    private String personalColor;

    @Column(nullable = false, length = 512)
    private String img;

    @Column(nullable = false, length = 512)
    private String icon;

    @Column(nullable = false, length = 512)
    private String background;

    @Column(name = "three_sides", nullable = false, length = 512)
    private String threeSides;

    @Column(name = "youtube_url", nullable = false, length = 512)
    private String youtubeUrl;

    @Column(name = "youtube_channel_id", nullable = false, length = 64)
    private String youtubeChannelId;

    @Column(name = "youtube_playlist_id", nullable = false, length = 64)
    private String youtubePlaylistId;

    @Column(name = "twitter_url", nullable = false, length = 512)
    private String twitterUrl;
}