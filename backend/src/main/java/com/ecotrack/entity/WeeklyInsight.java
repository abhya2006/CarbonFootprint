package com.ecotrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Entity @Table(name = "weekly_insights", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "week_start"})) @Getter @Setter
public class WeeklyInsight {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(name = "week_start", nullable = false) private LocalDate weekStart;
    @Column(name = "week_end", nullable = false) private LocalDate weekEnd;
    @Column(nullable = false) private double totalCo2e;
    @Column(nullable = false) private double previousWeekCo2e;
    @Column(nullable = false) private double percentageChange;
    @Column(nullable = false) private String topCategory;
    @Column(nullable = false, length = 500) private String recommendation;
    @Column(nullable = false) private Instant generatedAt = Instant.now();
}