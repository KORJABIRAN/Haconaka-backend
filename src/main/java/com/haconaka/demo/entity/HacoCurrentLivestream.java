package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "haco_currentlivestream")
public class HacoCurrentLivestream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "c_pk")
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "c_m_pk", nullable = false)
    private Integer memberPk;

    @Column(name = "c_address", nullable = false, length = 100)
    private String address;
}
