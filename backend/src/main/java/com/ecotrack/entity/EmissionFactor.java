package com.ecotrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "emission_factors", uniqueConstraints = @UniqueConstraint(columnNames = {"category", "activity_type", "unit"}))
@Getter @Setter
public class EmissionFactor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String category;
    @Column(name = "activity_type", nullable = false) private String activityType;
    @Column(nullable = false) private String unit;
    @Column(nullable = false) private double emissionFactor;
    private String source;
    private int sourceYear;
    @Column(nullable = false) private boolean active = true;
}