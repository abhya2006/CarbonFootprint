package com.ecotrack.service;

import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class OrganizationService {
    private final OrganizationMemberRepository members;
    private final ActivityRepository activities;
    public List<Map<String, Object>> members(String email) {
        var organizationId = organizationId(email);
        return members.findByOrganizationId(organizationId).stream().map(m -> Map.<String, Object>of(
            "name", m.getUser().getName(), "email", m.getUser().getEmail(), "role", m.getRole())).toList();
    }
    public Map<String, Object> dashboard(String email) {
        var organizationId = organizationId(email);
        var organization = members.findByOrganizationId(organizationId);
        var ids = organization.stream().map(m -> m.getUser().getId()).toList();
        LocalDate today = LocalDate.now(), monthStart = today.withDayOfMonth(1), previousStart = monthStart.minusMonths(1);
        var currentMembers = ids.isEmpty() ? List.<Object[]>of() : activities.memberTotals(ids, monthStart, today);
        LocalDate previousEnd = previousStart.plusDays(Math.min(today.getDayOfMonth(), previousStart.lengthOfMonth()) - 1L);
        var previousMembers = ids.isEmpty() ? List.<Object[]>of() : activities.memberTotals(ids, previousStart, previousEnd);
        double current = currentMembers.stream().mapToDouble(row -> ((Number) row[2]).doubleValue()).sum();
        double previous = previousMembers.stream().mapToDouble(row -> ((Number) row[2]).doubleValue()).sum();
        var categoryTotals = new HashMap<String, Double>();
        if (!ids.isEmpty()) activities.memberCategoryTotals(ids, monthStart, today).forEach(row -> categoryTotals.put((String) row[0], ((Number) row[1]).doubleValue()));
        var categoryRows = List.of("TRANSPORT", "ELECTRICITY", "FOOD", "SHOPPING").stream().map(category -> Map.<String, Object>of(
            "category", category, "co2e", Math.round(categoryTotals.getOrDefault(category, 0.0) * 100) / 100.0)).toList();
        var employeeRows = currentMembers.stream().map(row -> Map.<String, Object>of("name", row[1], "co2e", row[2])).toList();
        return Map.of("organizationName", organization.get(0).getOrganization().getName(), "totalCo2e", current,
            "previousMonthCo2e", previous, "monthlyReductionPercentage", previous == 0 ? 0 : Math.round((previous - current) / previous * 1000) / 10.0,
            "employeeCount", organization.size(), "averageEmployeeCo2e", organization.isEmpty() ? 0 : current / organization.size(),
            "categoryBreakdown", categoryRows, "employees", employeeRows);
    }
    private long organizationId(String email) {
        return members.findFirstByUserEmail(email).map(m -> m.getOrganization().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No organization membership found"));
    }
}