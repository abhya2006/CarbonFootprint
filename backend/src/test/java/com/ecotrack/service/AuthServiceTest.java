package com.ecotrack.service;

import com.ecotrack.dto.ApiDtos.LoginRequest;
import com.ecotrack.dto.ApiDtos.RegisterRequest;
import com.ecotrack.entity.User;
import com.ecotrack.repository.UserRepository;
import com.ecotrack.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {
    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService service;

    @Test
    void registerTrimsNameNormalizesEmailAndReturnsToken() {
        when(users.existsByEmail("person@example.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });
        when(jwtService.generate(any(User.class))).thenReturn("signed-token");

        var response = service.register(new RegisterRequest("  Person  ", "PERSON@example.com", "password123"));

        assertEquals("signed-token", response.token());
        assertEquals(3L, response.user().id());
        assertEquals("Person", response.user().name());
        assertEquals("person@example.com", response.user().email());
        verify(users).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmail("person@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.register(new RegisterRequest("Person", "PERSON@example.com", "password123")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void loginAuthenticatesNormalizedEmailAndReturnsUser() {
        User user = new User();
        user.setId(3L);
        user.setName("Person");
        user.setEmail("person@example.com");
        user.setRole("USER");
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generate(user)).thenReturn("signed-token");

        var response = service.login(new LoginRequest("PERSON@example.com", "password123"));

        assertEquals("signed-token", response.token());
        assertEquals("person@example.com", response.user().email());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}