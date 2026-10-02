package com.ecotrack.service;

import com.ecotrack.entity.User;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.repository.WeeklyInsightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class WeeklyInsightServiceTest {
    @Mock
    private UserRepository users;

    @Mock
    private ActivityRepository activities;

    @Mock
    private WeeklyInsightRepository insights;

    @InjectMocks
    private WeeklyInsightService service;

    @Test
    void currentBuildsAndSavesInsightForTopCategory() {
        User user = new User();
        user.setId(6L);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate previousWeekEnd = today.minusWeeks(1);
        LocalDate previousWeekStart = previousWeekEnd.with(DayOfWeek.MONDAY);
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(activities.totalBetween(6L, weekStart, today)).thenReturn(18.0);
        when(activities.totalBetween(6L, previousWeekStart, previousWeekEnd)).thenReturn(20.0);
        when(activities.categoryTotals(6L, weekStart, today)).thenReturn(
                List.<Object[]>of(new Object[]{"FOOD", 4.0}, new Object[]{"TRANSPORT", 14.0}));
        when(insights.findByUserIdAndWeekStart(6L, weekStart)).thenReturn(Optional.empty());

        var result = service.current("person@example.com");

        assertEquals("TRANSPORT", result.get("topCategory"));
        assertEquals(18.0, result.get("totalCo2e"));
        assertEquals(10.0, result.get("percentageChange"));
        assertEquals("Consider one transit, shared, cycling, or walking trip this week.", result.get("recommendation"));
        verify(insights).save(any(com.ecotrack.entity.WeeklyInsight.class));
    }

    @Test
    void currentUsesFallbackWhenThereAreNoActivityCategories() {
        User user = new User();
        user.setId(6L);
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(activities.totalBetween(any(), any(), any())).thenReturn(0.0);
        when(activities.categoryTotals(any(), any(), any())).thenReturn(List.of());
        when(insights.findByUserIdAndWeekStart(any(), any())).thenReturn(Optional.empty());

        var result = service.current("person@example.com");

        assertEquals("NONE", result.get("topCategory"));
        assertEquals(0.0, result.get("percentageChange"));
        assertEquals("Log a few activities to build a clearer picture for next week's summary.", result.get("recommendation"));
    }

    @Test
    void currentRejectsUnknownUser() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.current("missing@example.com"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}