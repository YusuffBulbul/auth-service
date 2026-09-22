package com.yusuf.taskmanagement.authservice.service;

import com.yusuf.taskmanagement.authservice.dto.AuthResponse;
import com.yusuf.taskmanagement.authservice.dto.LoginRequest;
import com.yusuf.taskmanagement.authservice.dto.RegisterRequest;
import com.yusuf.taskmanagement.authservice.model.AppUser;
import com.yusuf.taskmanagement.authservice.model.Role;
import com.yusuf.taskmanagement.authservice.repository.UserRepository;
import com.yusuf.taskmanagement.authservice.security.JwtService;
import com.yusuf.taskmanagement.authservice.exception.DuplicateEmailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(
                    "A user with this email already exists"
            );
        }

        AppUser user = AppUser.builder()
                .name(request.name().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.USER))
                .build();

        AppUser savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        AppUser user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Email or password is incorrect"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new BadCredentialsException(
                    "Email or password is incorrect"
            );
        }

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(AppUser user) {
        String accessToken = jwtService.generateToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}