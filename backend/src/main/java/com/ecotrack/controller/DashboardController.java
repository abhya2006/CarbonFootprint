package com.ecotrack.controller;

import com.ecotrack.service.DashboardService;
import com.ecotrack.service.OrganizationService;
import com.ecotrack.service.WeeklyInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service; private final OrganizationService organizationService; private final WeeklyInsightService weeklyInsightService;
    @GetMapping("/dashboard/summary") public Map<String, Object> summary(Authentication auth) { return service.summary(auth.getName()); }
    @GetMapping("/dashboard/category-breakdown") public List<Map<String, Object>> categories(Authentication auth) { return service.categories(auth.getName()); }
    @GetMapping("/dashboard/weekly-trend") public List<Map<String, Object>> trend(Authentication auth) { return service.weeklyTrend(auth.getName()); }
    @GetMapping("/dashboard/monthly-comparison") public Map<String, Object> comparison(Authentication auth) { return service.monthlyComparison(auth.getName()); }
    @GetMapping("/dashboard/benchmark") public Map<String, Object> benchmark(Authentication auth) { return service.benchmark(auth.getName()); }
    @GetMapping("/dashboard/insights") public List<Map<String, Object>> insights(Authentication auth) { return service.insights(auth.getName()); }
    @GetMapping("/insights/weekly") public Map<String, Object> weeklyInsight(Authentication auth) { return weeklyInsightService.current(auth.getName()); }
    @GetMapping("/leaderboard") public List<Map<String, Object>> leaderboard() { return service.leaderboard(); }
    @GetMapping("/organization/dashboard") public Map<String, Object> organization(Authentication auth) { return organizationService.dashboard(auth.getName()); }
    @GetMapping("/organization/members") public List<Map<String, Object>> members(Authentication auth) { return organizationService.members(auth.getName()); }
    @GetMapping("/organization/footprint") public Map<String, Object> footprint(Authentication auth) { return organizationService.dashboard(auth.getName()); }
    @GetMapping("/organization/trends") public List<Map<String, Object>> trends(Authentication auth) { return List.of(organizationService.dashboard(auth.getName())); }
}