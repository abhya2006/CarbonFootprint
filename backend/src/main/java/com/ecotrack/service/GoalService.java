package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.*;
import com.ecotrack.entity.SustainabilityGoal;
import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goals; private final UserRepository users; private final ActivityRepository activities; private final BadgeAwardService badgeAwards;
    public GoalResponse create(String email, GoalRequest request) { return save(null, email, request); }
    public GoalResponse update(String email, long id, GoalRequest request) { return save(id, email, request); }
    private GoalResponse save(Long id, String email, GoalRequest request) {
        if (request.endDate().isBefore(request.startDate())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        var user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var goal = id == null ? new SustainabilityGoal() : goals.findByIdAndUserId(id, user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found"));
        goal.setUser(user); goal.setTitle(request.title()); goal.setTargetReductionPercentage(request.targetReductionPercentage());
        goal.setTargetCo2e(request.targetCo2e()); goal.setStartDate(request.startDate()); goal.setEndDate(request.endDate());
        goal.setStatus(LocalDate.now().isAfter(request.endDate()) ? "COMPLETED" : "ACTIVE");
        SustainabilityGoal saved = goals.save(goal);
        GoalResponse response = map(saved);
        badgeAwards.evaluateGoal(saved, response.currentCo2e());
        return response;
    }
    public List<GoalResponse> list(String email) { long userId = userId(email); return goals.findByUserIdOrderByEndDateAsc(userId).stream().map(this::map).toList(); }
    public GoalResponse get(String email, long id) { return goals.findByIdAndUserId(id, userId(email)).map(this::map).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found")); }
    public void delete(String email, long id) { goals.delete(goals.findByIdAndUserId(id, userId(email)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found"))); }
    private long userId(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId(); }
    private GoalResponse map(SustainabilityGoal goal) {
        double current = activities.totalBetween(goal.getUser().getId(), goal.getStartDate(), goal.getEndDate());
        double progress = goal.getTargetCo2e() == 0 ? 0 : Math.min(100, current / goal.getTargetCo2e() * 100);
        return new GoalResponse(goal.getId(), goal.getTitle(), goal.getTargetReductionPercentage(), goal.getTargetCo2e(), current,
            Math.max(0, goal.getTargetCo2e() - current), Math.round(progress * 10) / 10.0, goal.getStartDate(), goal.getEndDate(), goal.getStatus());
    }
}