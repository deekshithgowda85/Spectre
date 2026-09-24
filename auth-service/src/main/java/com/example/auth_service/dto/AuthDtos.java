package com.example.auth_service.dto;

import com.example.auth_service.model.AppUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 72) String password
    ) { }

    public record LoginRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank String password
    ) { }

    public record UserResponse(Long id, String name, String email, String role) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
        }
    }

    public record LoginResponse(String accessToken, UserResponse user) { }
}
