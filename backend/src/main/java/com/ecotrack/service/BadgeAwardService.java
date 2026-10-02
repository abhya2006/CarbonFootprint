package com.ecotrack.service;

import com.ecotrack.entity.*;
import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service @RequiredArgsConstructor
public class BadgeAwardService {
    private final BadgeRepository badges;
    private final UserBadgeRepository userBadges;
    private final ActivityRepository activities;

    @Transactional
    public void evaluateActivity(Activity activity) {
        Long userId = activity.getUser().getId();
        if (activities.countByUserId(userId) >= 1) award(activity.getUser(), "FIRST_LOG");
        if ("TRANSPORT".equals(activity.getCategory()) &&
                java.util.Set.of("BUS", "TRAIN", "BIKE", "WALKING").contains(activity.getActivityType()))
            award(activity.getUser(), "ECO_COMMUTER");
        if ("ELECTRICITY".equals(activity.getCategory()) && activity.getQuantity() <= 3)
            award(activity.getUser(), "ENERGY_SAVER");
        if (activity.getCo2e() <= 1) award(activity.getUser(), "LOW_CARBON");
        LocalDate today = LocalDate.now();
        if (activities.dailyTotals(userId, today.minusDays(6), today).size() == 7)
            award(activity.getUser(), "WEEKLY_STREAK");
    }

    @Transactional
    public void evaluateGoal(SustainabilityGoal goal, double currentCo2e) {
        if (!LocalDate.now().isBefore(goal.getEndDate()) && currentCo2e <= goal.getTargetCo2e())
            award(goal.getUser(), "GOAL_CRUSHER");
    }

    public void award(User user, String code) {
        if (userBadges.existsByUserIdAndBadgeCode(user.getId(), code)) return;
        badges.findByCode(code).ifPresent(badge -> {
            UserBadge earned = new UserBadge(); earned.setUser(user); earned.setBadge(badge); userBadges.save(earned);
        });
    }
}