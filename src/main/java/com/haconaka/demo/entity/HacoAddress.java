package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "haco_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class HacoAddress {

    @Id
    @Column(name = "a_pk", nullable = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "a_m_pk", nullable = false)
    private Integer memberPk;

    @Column(name = "a_category", nullable = false, length = 30)
    private String category;

    @Column(name = "a_address", nullable = false, length = 200)
    private String address;
}
