package com.example.auth_service.service;

import com.example.auth_service.dto.AuthDtos.LoginRequest;
import com.example.auth_service.dto.AuthDtos.LoginResponse;
import com.example.auth_service.dto.AuthDtos.RegisterRequest;
import com.example.auth_service.dto.AuthDtos.UserResponse;
import com.example.auth_service.model.AppUser;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Email is already registered");
        }
        AppUser user = users.save(new AppUser(request.name().trim(), email,
            passwordEncoder.encode(request.password())));
        return UserResponse.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return new LoginResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    public UserResponse currentUser(String email) {
        return users.findByEmailIgnoreCase(email).map(UserResponse::from)
            .orElseThrow(() -> new BadCredentialsException("Authenticated user no longer exists"));
    }
}
