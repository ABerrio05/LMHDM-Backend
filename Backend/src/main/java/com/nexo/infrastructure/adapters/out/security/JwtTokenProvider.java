package com.nexo.infrastructure.adapters.out.security;

import com.nexo.application.ports.out.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProviderPort {
    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${app.security.jwt-secret}") String secret,
                            @Value("${app.security.jwt-expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String createToken(Long userId, String email) {
        Date now = new Date();
        return Jwts.builder().subject(userId.toString()).claim("email", email).issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs)).signWith(key).compact();
    }

    @Override
    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AuthenticatedUser(Long.valueOf(claims.getSubject()), claims.get("email", String.class)));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
