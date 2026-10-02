package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.*;
import com.ecotrack.entity.Activity;
import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activities; private final UserRepository users; private final EmissionCalculationService calculator; private final BadgeAwardService badgeAwards;
    public ActivityResponse create(String email, ActivityRequest request) { return save(null, email, request); }
    public ActivityResponse update(String email, long id, ActivityRequest request) { return save(id, email, request); }
    private ActivityResponse save(Long id, String email, ActivityRequest request) {
        var user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var calculation = calculator.calculate(request.category().toUpperCase(), request.activityType().toUpperCase(), request.quantity(), request.unit().toUpperCase());
        Activity activity = id == null ? new Activity() : owned(id, user.getId());
        activity.setUser(user); activity.setCategory(request.category().toUpperCase()); activity.setActivityType(request.activityType().toUpperCase());
        activity.setQuantity(request.quantity()); activity.setUnit(request.unit().toUpperCase()); activity.setCo2e(calculation.co2e());
        activity.setActivityDate(request.activityDate()); activity.setDescription(request.description());
        Activity saved = activities.save(activity);
        badgeAwards.evaluateActivity(saved);
        return map(saved);
    }
    public List<ActivityResponse> list(String email, String category, LocalDate from, LocalDate to) {
        var user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        List<Activity> found = from != null && to != null
            ? activities.findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(user.getId(), from, to)
            : activities.findByUserIdOrderByActivityDateDesc(user.getId());
        return found.stream().filter(a -> category == null || a.getCategory().equalsIgnoreCase(category)).map(this::map).toList();
    }
    public ActivityResponse get(String email, long id) { return map(owned(id, userId(email))); }
    public void delete(String email, long id) { activities.delete(owned(id, userId(email))); }
    public CalculationResponse calculate(ActivityRequest request) { return calculator.calculate(request.category().toUpperCase(), request.activityType().toUpperCase(), request.quantity(), request.unit().toUpperCase()); }
    private Long userId(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId(); }
    private Activity owned(long id, long userId) { return activities.findById(id).filter(a -> a.getUser().getId().equals(userId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found")); }
    private ActivityResponse map(Activity a) { return new ActivityResponse(a.getId(), a.getCategory(), a.getActivityType(), a.getQuantity(), a.getUnit(), a.getCo2e(), a.getActivityDate(), a.getDescription()); }
}