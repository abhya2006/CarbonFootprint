package com.ecotrack.service;

import com.ecotrack.entity.WeeklyInsight;
import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class WeeklyInsightService {
    private final UserRepository users; private final ActivityRepository activities; private final WeeklyInsightRepository insights;
    @Transactional
    public Map<String, Object> current(String email) {
        var user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        LocalDate end = LocalDate.now(), start = end.with(DayOfWeek.MONDAY);
        LocalDate previousEnd = end.minusWeeks(1), previousStart = previousEnd.with(DayOfWeek.MONDAY);
        double currentTotal = activities.totalBetween(user.getId(), start, end);
        double previousTotal = activities.totalBetween(user.getId(), previousStart, previousEnd);
        double change = previousTotal == 0 ? 0 : Math.round((previousTotal - currentTotal) / previousTotal * 1000) / 10.0;
        var categoryRows = activities.categoryTotals(user.getId(), start, end);
        String topCategory = categoryRows.stream().max(Comparator.comparingDouble(row -> ((Number) row[1]).doubleValue()))
            .map(row -> (String) row[0]).orElse("NONE");
        String recommendation = switch (topCategory) {
            case "TRANSPORT" -> "Consider one transit, shared, cycling, or walking trip this week.";
            case "ELECTRICITY" -> "Try one small energy-saving change and compare your next weekly total.";
            case "FOOD" -> "Try a lower-impact meal option once this week and see how it fits.";
            case "SHOPPING" -> "Pause before your next purchase and consider repair, reuse, or borrowing.";
            default -> "Log a few activities to build a clearer picture for next week's summary.";
        };
        WeeklyInsight insight = insights.findByUserIdAndWeekStart(user.getId(), start).orElseGet(WeeklyInsight::new);
        insight.setUser(user); insight.setWeekStart(start); insight.setWeekEnd(end); insight.setTotalCo2e(currentTotal);
        insight.setPreviousWeekCo2e(previousTotal); insight.setPercentageChange(change); insight.setTopCategory(topCategory);
        insight.setRecommendation(recommendation); insight.setGeneratedAt(Instant.now()); insights.save(insight);
        return Map.of("weekStart", start.toString(), "weekEnd", end.toString(), "totalCo2e", currentTotal,
            "previousWeekCo2e", previousTotal, "percentageChange", change, "topCategory", topCategory, "recommendation", recommendation);
    }
}