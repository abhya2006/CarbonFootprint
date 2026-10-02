package com.ecotrack.controller;

import com.ecotrack.dto.ApiDtos.*;
import com.ecotrack.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/activities") @RequiredArgsConstructor
public class ActivityController {
    private final ActivityService service;
    @PostMapping public ActivityResponse create(Authentication auth, @Valid @RequestBody ActivityRequest request) { return service.create(auth.getName(), request); }
    @PostMapping("/calculate") public CalculationResponse calculate(@Valid @RequestBody ActivityRequest request) { return service.calculate(request); }
    @GetMapping public List<ActivityResponse> list(Authentication auth, @RequestParam(required = false) String category,
        @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) { return service.list(auth.getName(), category, from, to); }
    @GetMapping("/{id}") public ActivityResponse get(Authentication auth, @PathVariable long id) { return service.get(auth.getName(), id); }
    @PutMapping("/{id}") public ActivityResponse update(Authentication auth, @PathVariable long id, @Valid @RequestBody ActivityRequest request) { return service.update(auth.getName(), id, request); }
    @DeleteMapping("/{id}") public void delete(Authentication auth, @PathVariable long id) { service.delete(auth.getName(), id); }
}