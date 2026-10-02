package com.ecotrack.repository;

import com.ecotrack.entity.SustainabilityGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<SustainabilityGoal, Long> {
    List<SustainabilityGoal> findByUserIdOrderByEndDateAsc(Long userId);
    Optional<SustainabilityGoal> findByIdAndUserId(Long id, Long userId);
}