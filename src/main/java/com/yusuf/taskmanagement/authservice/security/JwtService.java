package com.yusuf.taskmanagement.authservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.yusuf.taskmanagement.authservice.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;

        if (this.secret.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }
    }

    public String generateToken(AppUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

        List<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .sorted()
                .toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("task-management-auth-service")
                .subject(user.getId())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        try {
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException(
                    "JWT could not be generated",
                    exception
            );
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}