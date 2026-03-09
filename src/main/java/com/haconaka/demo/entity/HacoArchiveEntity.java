package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "haco_archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class HacoArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // a_pk 오토인크리먼트
    @Column(name = "a_pk", nullable = false)
    private Integer id;

    @Column(name = "a_m_pk", nullable = false)
    private Integer memberId;

    @Column(name = "a_date", nullable = false)
    private LocalDate date;

    @Column(name = "a_time", nullable = false)
    private LocalTime time;

    @Column(name = "a_collabo", length = 20)
    private String collabo;

    @Column(name = "a_collabomember", length = 500)
    private String collaboMember;

    @Column(name = "a_category", length = 50)
    private String category;

    @Column(name = "a_title", length = 200)
    private String title;

    @Column(name = "a_thumbnail", length = 150)
    private String thumbnail;

    @Column(name = "a_videoid", length = 200)
    private String videoId;
}
