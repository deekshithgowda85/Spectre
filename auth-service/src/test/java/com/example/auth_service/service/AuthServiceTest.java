package com.example.auth_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.auth_service.dto.AuthDtos.LoginRequest;
import com.example.auth_service.dto.AuthDtos.RegisterRequest;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository users;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    @Test
    void rejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> authService.register(
            new RegisterRequest("User", "user@example.com", "password123")));
    }

    @Test
    void rejectsInvalidPassword() {
        AppUser user = new AppUser("User", "user@example.com", new BCryptPasswordEncoder().encode("correct"));
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> authService.login(
            new LoginRequest("user@example.com", "wrong")));
    }

    @Test
    void registersHashedPasswordAndReturnsSafeUser() {
        when(users.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        var response = authService.register(new RegisterRequest("User", "user@example.com", "password123"));
        assertEquals("user@example.com", response.email());
        assertEquals("USER", response.role());
    }
}
