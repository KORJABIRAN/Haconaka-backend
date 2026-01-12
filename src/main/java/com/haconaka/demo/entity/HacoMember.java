package com.haconaka.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "haco_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HacoMember {

    @Id
    @Column(name = "m_pk", nullable = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "m_name", nullable = false, length = 30)
    private String name;

    @Column(name = "m_gen", nullable = false)
    private Integer gen;

    @Column(name = "m_birth", nullable = false)
    private java.time.LocalDate birth;

    @Column(name = "m_debut", nullable = false)
    private java.time.LocalDate debut;

    @Column(name = "m_mother_name", nullable = false, length = 100)
    private String motherName;

    @Column(name = "m_mother_twitter", nullable = false, length = 100)
    private String motherTwitter;

    @Column(name = "m_introduce", nullable = false, length = 1000)
    private String introduce;

    @Column(name = "m_personalcolor", nullable = false, length = 20)
    private String personalColor;

    @Column(name = "m_audio", length = 100)
    private String audio; // nullable

}