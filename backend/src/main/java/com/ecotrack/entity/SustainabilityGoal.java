package com.ecotrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "sustainability_goals", indexes = @Index(name = "idx_goal_user", columnList = "user_id"))
@Getter @Setter
public class SustainabilityGoal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false) private String title;
    private double targetReductionPercentage;
    @Column(nullable = false) private double targetCo2e;
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Column(nullable = false) private String status = "ACTIVE";
}