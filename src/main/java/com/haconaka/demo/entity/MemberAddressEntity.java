package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAddressEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false, length = 512)
    private String url;
}