package com.ecotrack.repository;

import com.ecotrack.entity.EmissionFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {
    Optional<EmissionFactor> findFirstByCategoryIgnoreCaseAndActivityTypeIgnoreCaseAndUnitIgnoreCaseAndActiveTrue(String category, String activityType, String unit);
}