package com.ecotrack.service;

import com.ecotrack.entity.EmissionFactor;
import com.ecotrack.repository.EmissionFactorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmissionCalculationServiceTest {
    @Mock
    private EmissionFactorRepository factors;

    @InjectMocks
    private EmissionCalculationService service;

    @Test
    void calculatesFromTheConfiguredFactor() {
        EmissionFactor factor = new EmissionFactor();
        factor.setActivityType("CAR");
        factor.setUnit("KM");
        factor.setEmissionFactor(0.21);
        when(factors.findFirstByCategoryIgnoreCaseAndActivityTypeIgnoreCaseAndUnitIgnoreCaseAndActiveTrue(
                "TRANSPORT", "CAR", "KM")).thenReturn(Optional.of(factor));

        var result = service.calculate("TRANSPORT", "CAR", 25, "KM");

        assertEquals("CAR", result.activityType());
        assertEquals(5.25, result.co2e());
        assertEquals(0.21, result.emissionFactor());
    }

    @Test
    void roundsCalculatedCo2eToTwoDecimalPlaces() {
        EmissionFactor factor = new EmissionFactor();
        factor.setActivityType("DRIVING");
        factor.setUnit("KM");
        factor.setEmissionFactor(0.2345);
        when(factors.findFirstByCategoryIgnoreCaseAndActivityTypeIgnoreCaseAndUnitIgnoreCaseAndActiveTrue(
                "transport", "driving", "km")).thenReturn(Optional.of(factor));

        var result = service.calculate("transport", "driving", 10, "km");

        assertEquals(2.35, result.co2e());
        assertEquals(0.2345, result.emissionFactor());
    }

    @Test
    void rejectsAnActivityWithoutAnActiveFactor() {
        when(factors.findFirstByCategoryIgnoreCaseAndActivityTypeIgnoreCaseAndUnitIgnoreCaseAndActiveTrue(
                "TRANSPORT", "SCOOTER", "KM")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.calculate("TRANSPORT", "SCOOTER", 4, "KM"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("No active emission factor matches this category, activity, and unit", exception.getReason());
    }
}