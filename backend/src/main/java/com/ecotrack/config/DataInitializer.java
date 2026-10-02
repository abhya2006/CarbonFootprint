package com.ecotrack.config;

import com.ecotrack.entity.*;
import com.ecotrack.repository.*;
import com.ecotrack.service.BadgeAwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final EmissionFactorRepository factors; private final UserRepository users; private final ActivityRepository activities;
    private final GoalRepository goals; private final OrganizationRepository organizations; private final OrganizationMemberRepository members;
    private final BadgeRepository badges; private final BadgeAwardService badgeAwards; private final PasswordEncoder encoder;
    @Override public void run(String... args) {
        seedFactors();
        seedBadges();
        User demo = user("demo@ecotrack.com", "Alex Morgan", "USER");
        User admin = user("admin@ecotrack.com", "EcoTrack Admin", "ADMIN");
        User orgAdmin = user("orgadmin@ecotrack.com", "Jamie Rivera", "ORG_ADMIN");
        if (activities.count() == 0) seedActivities(demo, orgAdmin);
        if (goals.count() == 0) { SustainabilityGoal goal = new SustainabilityGoal(); goal.setUser(demo); goal.setTitle("Keep this month's footprint under 180 kg"); goal.setTargetReductionPercentage(20); goal.setTargetCo2e(180); goal.setStartDate(LocalDate.now().withDayOfMonth(1)); goal.setEndDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())); goals.save(goal); }
        if (organizations.count() == 0) {
            Organization organization = new Organization(); organization.setName("Canopy Studio"); organization.setDescription("Demo organization workspace"); organizations.save(organization);
            membership(organization, orgAdmin, "ORG_ADMIN"); membership(organization, demo, "MEMBER"); membership(organization, admin, "ORG_ADMIN");
        }
    }
    private User user(String email, String name, String role) {
        return users.findByEmail(email).orElseGet(() -> { User user = new User(); user.setName(name); user.setEmail(email); user.setPassword(encoder.encode("Demo@123")); user.setRole(role); return users.save(user); });
    }
    private void seedFactors() {
        if (factors.count() > 0) return;
        List<Object[]> rows = List.of(
            new Object[]{"TRANSPORT", "CAR", "KM", 0.21}, new Object[]{"TRANSPORT", "BUS", "KM", 0.10},
            new Object[]{"TRANSPORT", "TRAIN", "KM", 0.041}, new Object[]{"TRANSPORT", "BIKE", "KM", 0.0},
            new Object[]{"TRANSPORT", "WALKING", "KM", 0.0}, new Object[]{"ELECTRICITY", "ELECTRICITY", "KWH", 0.39},
            new Object[]{"FOOD", "BEEF", "MEAL", 5.5}, new Object[]{"FOOD", "CHICKEN", "MEAL", 1.7},
            new Object[]{"FOOD", "VEGETARIAN", "MEAL", 0.9}, new Object[]{"FOOD", "VEGAN", "MEAL", 0.55},
            new Object[]{"SHOPPING", "CLOTHING", "ITEM", 20.0}, new Object[]{"SHOPPING", "ELECTRONICS", "ITEM", 45.0},
            new Object[]{"SHOPPING", "FURNITURE", "ITEM", 65.0}, new Object[]{"SHOPPING", "GENERAL_PURCHASE", "ITEM", 8.0});
        for (Object[] row : rows) { EmissionFactor factor = new EmissionFactor(); factor.setCategory((String) row[0]); factor.setActivityType((String) row[1]); factor.setUnit((String) row[2]); factor.setEmissionFactor((double) row[3]); factor.setSource("EcoTrack illustrative reference factor"); factor.setSourceYear(2024); factors.save(factor); }
    }
    private void seedBadges() {
        List<String[]> definitions = List.of(
            new String[]{"FIRST_LOG", "First footprint", "Logged your first activity.", "leaf"},
            new String[]{"WEEKLY_STREAK", "A steady week", "Logged activities on seven consecutive days.", "calendar"},
            new String[]{"LOW_CARBON", "Lighter choice", "Recorded an activity at or below 1 kg CO2e.", "sparkles"},
            new String[]{"ECO_COMMUTER", "Eco commuter", "Logged a lower-emission way to travel.", "bike"},
            new String[]{"ENERGY_SAVER", "Energy mindful", "Logged an electricity activity of 3 kWh or less.", "zap"},
            new String[]{"GOAL_CRUSHER", "Goal achieved", "Met your footprint budget by the end of the goal period.", "target"});
        for (String[] definition : definitions) if (badges.findByCode(definition[0]).isEmpty()) {
            Badge badge = new Badge(); badge.setCode(definition[0]); badge.setName(definition[1]); badge.setDescription(definition[2]); badge.setIcon(definition[3]); badges.save(badge);
        }
    }
    private void seedActivities(User demo, User orgAdmin) {
        add(demo, "TRANSPORT", "CAR", 18, "KM", 5); add(demo, "TRANSPORT", "TRAIN", 24, "KM", 4);
        add(demo, "ELECTRICITY", "ELECTRICITY", 6, "KWH", 2); add(demo, "FOOD", "VEGETARIAN", 1, "MEAL", 1);
        add(demo, "FOOD", "CHICKEN", 1, "MEAL", 3); add(demo, "SHOPPING", "CLOTHING", 0.2, "ITEM", 6);
        add(orgAdmin, "TRANSPORT", "BUS", 12, "KM", 1); add(orgAdmin, "ELECTRICITY", "ELECTRICITY", 4, "KWH", 1);
    }
    private void add(User user, String category, String type, double quantity, String unit, int daysAgo) {
        var factor = factors.findFirstByCategoryIgnoreCaseAndActivityTypeIgnoreCaseAndUnitIgnoreCaseAndActiveTrue(category, type, unit).orElseThrow();
        Activity activity = new Activity(); activity.setUser(user); activity.setCategory(category); activity.setActivityType(type); activity.setQuantity(quantity);
        activity.setUnit(unit); activity.setCo2e(Math.round(quantity * factor.getEmissionFactor() * 100) / 100.0); activity.setActivityDate(LocalDate.now().minusDays(daysAgo));
        badgeAwards.evaluateActivity(activities.save(activity));
    }
    private void membership(Organization organization, User user, String role) { OrganizationMember member = new OrganizationMember(); member.setOrganization(organization); member.setUser(user); member.setRole(role); members.save(member); }
}