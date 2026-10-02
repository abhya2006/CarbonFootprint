package com.ecotrack.service;

import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class DashboardService {
    private final UserRepository users; private final ActivityRepository activities; private final GoalRepository goals; private final UserBadgeRepository userBadges;
    public Map<String, Object> summary(String email) {
        long id = userId(email); LocalDate today = LocalDate.now(); LocalDate week = today.with(DayOfWeek.MONDAY); LocalDate month = today.withDayOfMonth(1);
        double todayTotal = activities.totalBetween(id, today, today), weekly = activities.totalBetween(id, week, today);
        double monthly = activities.totalBetween(id, month, today);
        LocalDate previousMonthStart = month.minusMonths(1);
        LocalDate previousMonthEnd = previousMonthStart.plusDays(Math.min(today.getDayOfMonth(), previousMonthStart.lengthOfMonth()) - 1L);
        double previous = activities.totalBetween(id, previousMonthStart, previousMonthEnd);
        double change = previous == 0 ? 0 : (previous - monthly) / previous * 100;
        var top = categories(id, month, today).stream().filter(row -> ((Number) row.get("co2e")).doubleValue() > 0)
            .max(Comparator.comparingDouble(row -> ((Number) row.get("co2e")).doubleValue())).orElse(Map.of("category", "None", "co2e", 0.0));
        var activeGoal = goals.findByUserIdOrderByEndDateAsc(id).stream().filter(g -> g.getStatus().equals("ACTIVE")).findFirst();
        double dailyAverage = monthly / Math.max(1, today.getDayOfMonth());
        Map<String, Object> result = new LinkedHashMap<>(); result.put("todayCo2e", todayTotal); result.put("weeklyCo2e", weekly);
        result.put("monthlyCo2e", monthly); result.put("previousMonthCo2e", previous); result.put("reductionPercentage", Math.round(change * 10) / 10.0);
        result.put("topCategory", top.get("category")); result.put("dailyAverage", Math.round(dailyAverage * 100) / 100.0);
        result.put("goalProgress", activeGoal.map(g -> g.getTargetCo2e() == 0 ? 0 : Math.min(100, activities.totalBetween(id, g.getStartDate(), g.getEndDate()) / g.getTargetCo2e() * 100)).orElse(0.0));
        return result;
    }
    public List<Map<String, Object>> categories(String email) { return categories(userId(email), LocalDate.now().withDayOfMonth(1), LocalDate.now()); }
    private List<Map<String, Object>> categories(long id, LocalDate from, LocalDate to) {
        List<String> names = List.of("TRANSPORT", "ELECTRICITY", "FOOD", "SHOPPING");
        Map<String, Double> totals = new HashMap<>(); activities.categoryTotals(id, from, to).forEach(row -> totals.put((String) row[0], ((Number) row[1]).doubleValue()));
        double total = totals.values().stream().mapToDouble(Double::doubleValue).sum();
        return names.stream().map(name -> Map.<String, Object>of("category", name, "co2e", Math.round(totals.getOrDefault(name, 0.0) * 100) / 100.0,
            "percentage", total == 0 ? 0 : Math.round(totals.getOrDefault(name, 0.0) / total * 1000) / 10.0)).toList();
    }
    public List<Map<String, Object>> weeklyTrend(String email) {
        long id = userId(email); LocalDate end = LocalDate.now(); LocalDate start = end.with(DayOfWeek.MONDAY);
        Map<LocalDate, Double> totals = new HashMap<>(); activities.dailyTotals(id, start, end).forEach(row -> totals.put((LocalDate) row[0], ((Number) row[1]).doubleValue()));
        List<Map<String, Object>> days = new ArrayList<>(); for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1))
            days.add(Map.of("date", date.toString(), "day", date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH), "co2e", totals.getOrDefault(date, 0.0)));
        while (days.size() < 7) { int offset = days.size(); LocalDate date = start.plusDays(offset); days.add(Map.of("date", date.toString(), "day", date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH), "co2e", 0.0)); }
        return days;
    }
    public Map<String, Object> monthlyComparison(String email) {
        long id = userId(email); LocalDate today = LocalDate.now(); LocalDate start = today.withDayOfMonth(1); double current = activities.totalBetween(id, start, today);
        LocalDate previousStart = start.minusMonths(1); LocalDate previousEnd = previousStart.plusDays(Math.min(today.getDayOfMonth(), previousStart.lengthOfMonth()) - 1L);
        double previous = activities.totalBetween(id, previousStart, previousEnd);
        return Map.of("currentMonth", current, "previousMonth", previous);
    }
    public Map<String, Object> benchmark(String email) {
        long id = userId(email); LocalDate from = LocalDate.now().minusDays(6), to = LocalDate.now();
        double user = activities.totalBetween(id, from, to); Double average = activities.platformAverage(from, to);
        return Map.of("userCo2e", user, "platformAverage", average == null ? 0 : average, "periodDays", 7);
    }
    public List<Map<String, Object>> insights(String email) {
        var breakdown = categories(email); var total = breakdown.stream().mapToDouble(row -> ((Number) row.get("co2e")).doubleValue()).sum();
        var top = breakdown.stream().filter(row -> ((Number) row.get("co2e")).doubleValue() > 0)
            .max(Comparator.comparingDouble(row -> ((Number) row.get("co2e")).doubleValue())).orElse(Map.of("category", "NONE", "co2e", 0.0, "percentage", 0.0));
        String category = String.valueOf(top.get("category")); double percent = total == 0 ? 0 : ((Number) top.get("percentage")).doubleValue();
        String action = switch (category) {
            case "TRANSPORT" -> "Try combining nearby errands or replacing one car journey with transit this week.";
            case "ELECTRICITY" -> "Review high-use appliances and switch off standby devices when they are not needed.";
            case "FOOD" -> "Try one lower-impact meal this week and compare its footprint with your usual choice.";
            case "SHOPPING" -> "Consider repairing, borrowing, or choosing a durable second-hand option before your next purchase.";
            default -> "Log a few activities to reveal where small changes can make the biggest difference.";
        };
        return List.of(Map.of("type", "biggest_impact", "title", category + " is your largest category", "message", String.format(Locale.ROOT, "%s contributes %.1f%% of your logged footprint this month.", title(category), percent)),
            Map.of("type", "suggested_action", "title", "A practical next step", "message", action),
            Map.of("type", "weekly_summary", "title", "Your week so far", "message", String.format(Locale.ROOT, "You have logged %.2f kg CO2e since Monday.", activities.totalBetween(userId(email), LocalDate.now().with(DayOfWeek.MONDAY), LocalDate.now()))));
    }
    public List<Map<String, Object>> leaderboard() {
        LocalDate today = LocalDate.now(), start = today.with(DayOfWeek.MONDAY);
        List<Object[]> ranked = activities.leaderboard(start, today).stream().limit(20).toList();
        List<Long> ids = ranked.stream().map(row -> ((Number) row[0]).longValue()).toList();
        if (ids.isEmpty()) return List.of();
        Map<Long, Set<LocalDate>> activeDays = new HashMap<>();
        activities.memberDailyTotals(ids, today.minusDays(6), today).forEach(row ->
            activeDays.computeIfAbsent(((Number) row[0]).longValue(), ignored -> new HashSet<>()).add((LocalDate) row[1]));
        Map<Long, List<String>> earned = new HashMap<>();
        userBadges.findWithBadgesByUserIds(ids).forEach(item -> earned.computeIfAbsent(item.getUser().getId(), ignored -> new ArrayList<>()).add(item.getBadge().getName()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int index = 0; index < ranked.size(); index++) {
            Object[] row = ranked.get(index); long id = ((Number) row[0]).longValue();
            int streak = 0;
            for (LocalDate date = today; activeDays.getOrDefault(id, Set.of()).contains(date); date = date.minusDays(1)) streak++;
            result.add(Map.of("rank", index + 1, "name", row[1], "weeklyCo2e", row[2], "streak", streak,
                "badges", earned.getOrDefault(id, List.of())));
        }
        return result;
    }
    private long userId(String email) { return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId(); }
    private String title(String value) { return value.charAt(0) + value.substring(1).toLowerCase(Locale.ROOT); }
}