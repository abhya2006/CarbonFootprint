package com.ecotrack.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class ApiDtos {
    private ApiDtos() {}
    public record RegisterRequest(@NotBlank @Size(max = 100) String name, @Email @NotBlank String email, @NotBlank @Size(min = 8, max = 72) String password) {}
    public record UpdateProfileRequest(@NotBlank @Size(max = 100) String name, @Email @NotBlank String email) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, UserResponse user) {}
    public record UserResponse(Long id, String name, String email, String role) {}
    public record ActivityRequest(@NotBlank String category, @NotBlank String activityType, @Positive double quantity,
                                  @NotBlank String unit, @NotNull LocalDate activityDate, String description) {}
    public record ActivityResponse(Long id, String category, String activityType, double quantity, String unit,
                                   double co2e, LocalDate activityDate, String description) {}
    public record CalculationResponse(String activityType, double quantity, String unit, double emissionFactor, double co2e) {}
    public record GoalRequest(@NotBlank String title, @PositiveOrZero double targetReductionPercentage,
                              @Positive double targetCo2e, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
    public record GoalResponse(Long id, String title, double targetReductionPercentage, double targetCo2e,
                               double currentCo2e, double remaining, double percentageCompleted,
                               LocalDate startDate, LocalDate endDate, String status) {}
}