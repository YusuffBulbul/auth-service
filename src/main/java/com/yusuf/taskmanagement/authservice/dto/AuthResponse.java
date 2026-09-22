package com.yusuf.taskmanagement.authservice.dto;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String userId,
        String name,
        String email,
        Set<String> roles
) {
}