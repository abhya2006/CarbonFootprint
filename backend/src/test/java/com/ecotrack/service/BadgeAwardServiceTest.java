package com.ecotrack.service;

import com.ecotrack.entity.Badge;
import com.ecotrack.entity.User;
import com.ecotrack.entity.UserBadge;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.BadgeRepository;
import com.ecotrack.repository.UserBadgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeAwardServiceTest {
    @Mock
    private BadgeRepository badges;

    @Mock
    private UserBadgeRepository userBadges;

    @Mock
    private ActivityRepository activities;

    @InjectMocks
    private BadgeAwardService service;

    @Test
    void awardDoesNotCreateDuplicateUserBadge() {
        User user = user(4L);
        when(userBadges.existsByUserIdAndBadgeCode(4L, "FIRST_LOG")).thenReturn(true);

        service.award(user, "FIRST_LOG");

        verify(badges, never()).findByCode("FIRST_LOG");
        verify(userBadges, never()).save(any(UserBadge.class));
    }

    @Test
    void awardPersistsBadgeForUserWhenNotAlreadyEarned() {
        User user = user(4L);
        Badge badge = new Badge();
        badge.setCode("FIRST_LOG");
        when(userBadges.existsByUserIdAndBadgeCode(4L, "FIRST_LOG")).thenReturn(false);
        when(badges.findByCode("FIRST_LOG")).thenReturn(Optional.of(badge));

        service.award(user, "FIRST_LOG");

        ArgumentCaptor<UserBadge> captor = ArgumentCaptor.forClass(UserBadge.class);
        verify(userBadges).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
        assertEquals(badge, captor.getValue().getBadge());
    }

    @Test
    void evaluateActivityAwardsFirstLogEcoCommuterLowCarbonAndWeeklyStreak() {
        User user = user(4L);
        var activity = new com.ecotrack.entity.Activity();
        activity.setUser(user);
        activity.setCategory("TRANSPORT");
        activity.setActivityType("BIKE");
        activity.setQuantity(1);
        activity.setCo2e(0.5);
        when(activities.countByUserId(4L)).thenReturn(1L);
        when(activities.dailyTotals(any(), any(), any())).thenReturn(java.util.Collections.nCopies(7, new Object[0]));
        when(userBadges.existsByUserIdAndBadgeCode(4L, "FIRST_LOG")).thenReturn(false);
        when(userBadges.existsByUserIdAndBadgeCode(4L, "ECO_COMMUTER")).thenReturn(false);
        when(userBadges.existsByUserIdAndBadgeCode(4L, "LOW_CARBON")).thenReturn(false);
        when(userBadges.existsByUserIdAndBadgeCode(4L, "WEEKLY_STREAK")).thenReturn(false);
        when(badges.findByCode(any())).thenReturn(Optional.empty());

        service.evaluateActivity(activity);

        verify(badges).findByCode("FIRST_LOG");
        verify(badges).findByCode("ECO_COMMUTER");
        verify(badges).findByCode("LOW_CARBON");
        verify(badges).findByCode("WEEKLY_STREAK");
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}