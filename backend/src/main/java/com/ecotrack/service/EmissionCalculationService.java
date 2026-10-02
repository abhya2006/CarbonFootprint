package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.CalculationResponse;
import com.ecotrack.repository.EmissionFactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service @RequiredArgsConstructor
public class EmissionCalculationService {
    private final EmissionFactorRepository factors;
    public CalculationResponse calculate(String category, String activityType, double quantity, String unit) {
        var factor = factors.findFirstByCategoryIgnoreCaseAndActivityTypeIgnoreCaseAndUnitIgnoreCaseAndActiveTrue(category, activityType, unit)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active emission factor matches this category, activity, and unit"));
        double co2e = BigDecimal.valueOf(quantity).multiply(BigDecimal.valueOf(factor.getEmissionFactor()))
            .setScale(2, RoundingMode.HALF_UP).doubleValue();
        return new CalculationResponse(factor.getActivityType(), quantity, factor.getUnit(), factor.getEmissionFactor(), co2e);
    }
}