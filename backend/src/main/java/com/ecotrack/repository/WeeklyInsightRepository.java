package com.ecotrack.repository;

import com.ecotrack.entity.WeeklyInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyInsightRepository extends JpaRepository<WeeklyInsight, Long> {
    Optional<WeeklyInsight> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);
}