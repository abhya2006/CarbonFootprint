package com.ecotrack.controller;

import com.ecotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/badges") @RequiredArgsConstructor
public class BadgeController {
    private final UserRepository users; private final UserBadgeRepository badges;
    @GetMapping public List<Map<String, Object>> mine(Authentication authentication) {
        Long userId = users.findByEmail(authentication.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
        return badges.findWithBadgesByUserIds(List.of(userId)).stream().map(item -> Map.<String, Object>of(
            "code", item.getBadge().getCode(), "name", item.getBadge().getName(), "description", item.getBadge().getDescription(),
            "icon", item.getBadge().getIcon(), "awardedAt", item.getAwardedAt().toString())).toList();
    }
}