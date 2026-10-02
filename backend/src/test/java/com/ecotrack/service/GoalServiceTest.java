package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.GoalRequest;
import com.ecotrack.entity.User;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.GoalRepository;
import com.ecotrack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class GoalServiceTest {
    @Mock
    private GoalRepository goals;

    @Mock
    private UserRepository users;

    @Mock
    private ActivityRepository activities;

    @Mock
    private BadgeAwardService badgeAwards;

    @InjectMocks
    private GoalService service;

    @Test
    void createCalculatesGoalProgressAndClampsCompletedAmount() {
        User user = new User();
        user.setId(5L);
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now().plusDays(10);
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(activities.totalBetween(5L, start, end)).thenReturn(125.0);
        when(goals.save(any())).thenAnswer(invocation -> {
            var goal = invocation.getArgument(0, com.ecotrack.entity.SustainabilityGoal.class);
            goal.setId(9L);
            return goal;
        });

        var response = service.create("person@example.com",
                new GoalRequest("Reduce travel", 20, 100, start, end));

        assertEquals(9L, response.id());
        assertEquals(125, response.currentCo2e());
        assertEquals(0, response.remaining());
        assertEquals(100, response.percentageCompleted());
        assertEquals("ACTIVE", response.status());
        verify(badgeAwards).evaluateGoal(any(), anyDouble());
    }

    @Test
    void createRejectsEndDateBeforeStartDateBeforeLookingUpUser() {
        LocalDate start = LocalDate.of(2026, 9, 10);
        GoalRequest request = new GoalRequest("Reduce travel", 20, 100, start, start.minusDays(1));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create("person@example.com", request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("End date must be on or after start date", exception.getReason());
    }

    @Test
    void createMarksGoalCompletedAfterItsEndDate() {
        User user = new User();
        user.setId(5L);
        LocalDate start = LocalDate.now().minusDays(20);
        LocalDate end = LocalDate.now().minusDays(1);
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(activities.totalBetween(5L, start, end)).thenReturn(30.0);
        when(goals.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create("person@example.com",
                new GoalRequest("Reduce travel", 20, 100, start, end));

        assertEquals("COMPLETED", response.status());
        assertEquals(30, response.percentageCompleted());
    }
}