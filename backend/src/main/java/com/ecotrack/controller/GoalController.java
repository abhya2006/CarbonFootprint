package com.ecotrack.controller;

import com.ecotrack.dto.ApiDtos.*;
import com.ecotrack.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/goals") @RequiredArgsConstructor
public class GoalController {
    private final GoalService service;
    @PostMapping public GoalResponse create(Authentication auth, @Valid @RequestBody GoalRequest request) { return service.create(auth.getName(), request); }
    @GetMapping public List<GoalResponse> list(Authentication auth) { return service.list(auth.getName()); }
    @GetMapping("/{id}") public GoalResponse get(Authentication auth, @PathVariable long id) { return service.get(auth.getName(), id); }
    @PutMapping("/{id}") public GoalResponse update(Authentication auth, @PathVariable long id, @Valid @RequestBody GoalRequest request) { return service.update(auth.getName(), id, request); }
    @DeleteMapping("/{id}") public void delete(Authentication auth, @PathVariable long id) { service.delete(auth.getName(), id); }
}