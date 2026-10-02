package com.ecotrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "activities", indexes = {
    @Index(name = "idx_activity_user_date", columnList = "user_id,activity_date"),
    @Index(name = "idx_activity_category_type", columnList = "category,activity_type")
})
@Getter @Setter
public class Activity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false) private String category;
    @Column(name = "activity_type", nullable = false) private String activityType;
    @Column(nullable = false) private double quantity;
    @Column(nullable = false) private String unit;
    @Column(nullable = false) private double co2e;
    @Column(name = "activity_date", nullable = false) private LocalDate activityDate;
    private String description;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
}