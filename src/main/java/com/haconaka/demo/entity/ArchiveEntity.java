package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Column(name = "video_id", nullable = false, unique = true, length = 16)
    private String videoId;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 512)
    private String thumbnail;

    @Builder.Default
    @Column(nullable = false, length = 64)
    private String category = "NONE";

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Builder.Default
    @Column(name = "collabo_type", nullable = false, length = 32)
    private String collaboType = "NONE";

    @Column(name = "collabo_members", columnDefinition = "jsonb")
    private List<String> collaboMembers;
}