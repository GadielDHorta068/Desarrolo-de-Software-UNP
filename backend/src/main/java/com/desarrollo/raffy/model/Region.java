package com.desarrollo.raffy.model;


import org.locationtech.jts.geom.MultiPolygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor

@Entity
@Table(name = "region")

public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull(message = "el nombre es obligatorio")
    private String name;
    
    @Column(name = "region_type", nullable = false)
    @NotNull(message = "El tipo de region es obligatorio")
    @Enumerated(EnumType.STRING)
    private RegionType regionType;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    private Region parent;

    @Column(
        name = "geom",
        columnDefinition = "geometry(Multipolygon,4326)",
        nullable = false
    )
    @NotNull(message = "la geometria es obligatoria")
    private MultiPolygon geom; 
}
