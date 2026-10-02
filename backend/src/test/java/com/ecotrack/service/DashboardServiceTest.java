package com.ecotrack.service;

import com.ecotrack.entity.User;
import com.ecotrack.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock UserRepository users;
    @Mock ActivityRepository activities;
    @Mock GoalRepository goals;
    @Mock UserBadgeRepository userBadges;
    @InjectMocks DashboardService service;

    @Test void summaryUsesUserScopedTotalsAndReturnsStableCategory() {
        User user = new User(); user.setId(7L); user.setEmail("person@example.com");
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(activities.totalBetween(eq(7L), any(LocalDate.class), any(LocalDate.class))).thenReturn(3.5, 14.0, 28.0, 32.0);
        when(activities.categoryTotals(eq(7L), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.<Object[]>of(new Object[]{"TRANSPORT", 20.0}, new Object[]{"FOOD", 8.0}));
        when(goals.findByUserIdOrderByEndDateAsc(7L)).thenReturn(List.of());

        var summary = service.summary("person@example.com");

        assertEquals(3.5, summary.get("todayCo2e"));
        assertEquals("TRANSPORT", summary.get("topCategory"));
        assertEquals(32.0, summary.get("previousMonthCo2e"));
    }
}