package com.haconaka.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "haco_image")
public class HacoImage {

    @Id
    @Column(name = "i_m_pk", nullable = false)
    private Integer memberPk;

    @Column(name = "i_icon", nullable = false, length = 100)
    private String icon;

    @Column(name = "i_img", nullable = false, length = 100)
    private String img;

    @Column(name = "i_background", nullable = false, length = 100)
    private String background;

    @Column(name = "i_3side", nullable = false, length = 100)
    private String threeSide;
}
