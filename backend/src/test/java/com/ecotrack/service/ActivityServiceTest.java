package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.ActivityRequest;
import com.ecotrack.dto.ApiDtos.CalculationResponse;
import com.ecotrack.entity.Activity;
import com.ecotrack.entity.User;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
class ActivityServiceTest {
    @Mock
    private ActivityRepository activities;

    @Mock
    private UserRepository users;

    @Mock
    private EmissionCalculationService calculator;

    @Mock
    private BadgeAwardService badgeAwards;

    @InjectMocks
    private ActivityService service;

    @Test
    void createNormalizesActivityAndStoresCalculatedEmission() {
        User user = user(7L);
        LocalDate date = LocalDate.of(2026, 9, 1);
        ActivityRequest request = new ActivityRequest("transport", "car", 10, "km", date, "commute");
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(calculator.calculate("TRANSPORT", "CAR", 10, "KM"))
                .thenReturn(new CalculationResponse("CAR", 10, "KM", 0.2, 2));
        when(activities.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(12L);
            return activity;
        });

        var response = service.create("person@example.com", request);

        assertEquals(12L, response.id());
        assertEquals("TRANSPORT", response.category());
        assertEquals("CAR", response.activityType());
        assertEquals("KM", response.unit());
        assertEquals(2, response.co2e());
        assertEquals(date, response.activityDate());
        verify(activities).save(any(Activity.class));
        verify(badgeAwards).evaluateActivity(any(Activity.class));
    }

    @Test
    void listFiltersCategoriesWithoutChangingRepositoryOrdering() {
        User user = user(7L);
        Activity transport = activity(user, "TRANSPORT");
        Activity food = activity(user, "FOOD");
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(activities.findByUserIdOrderByActivityDateDesc(7L)).thenReturn(List.of(transport, food));

        var result = service.list("person@example.com", "transport", null, null);

        assertEquals(1, result.size());
        assertEquals("TRANSPORT", result.get(0).category());
    }

    @Test
    void getDoesNotExposeAnActivityOwnedByAnotherUser() {
        User owner = user(8L);
        Activity activity = activity(owner, "FOOD");
        activity.setId(12L);
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user(7L)));
        when(activities.findById(12L)).thenReturn(Optional.of(activity));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.get("person@example.com", 12L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void createRejectsAnUnknownUser() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        ActivityRequest request = new ActivityRequest("transport", "car", 1, "km", LocalDate.now(), null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create("missing@example.com", request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("person@example.com");
        return user;
    }

    private Activity activity(User user, String category) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setCategory(category);
        activity.setActivityType("CAR");
        activity.setQuantity(1);
        activity.setUnit("KM");
        activity.setCo2e(0.2);
        activity.setActivityDate(LocalDate.of(2026, 9, 1));
        return activity;
    }
}