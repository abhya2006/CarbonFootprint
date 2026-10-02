package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.*;
import com.ecotrack.entity.User;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager authenticationManager; private final JwtService jwtService;
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmail(request.email().toLowerCase())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        User user = new User(); user.setName(request.name().trim()); user.setEmail(request.email().toLowerCase()); user.setPassword(encoder.encode(request.password()));
        users.save(user); return response(user);
    }
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        return response(users.findByEmail(request.email().toLowerCase()).orElseThrow());
    }
    public UserResponse me(String email) { return users.findByEmail(email).map(this::userResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }
    public AuthResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String updatedEmail = request.email().toLowerCase();
        if (!user.getEmail().equals(updatedEmail) && users.existsByEmail(updatedEmail))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        user.setName(request.name().trim()); user.setEmail(updatedEmail);
        return response(users.save(user));
    }
    private AuthResponse response(User user) { return new AuthResponse(jwtService.generate(user), userResponse(user)); }
    private UserResponse userResponse(User user) { return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()); }
}