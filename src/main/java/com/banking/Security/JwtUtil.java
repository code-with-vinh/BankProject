package com.banking.Security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.secret:1234}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String emailFromClaim = claims.get("email", String.class);
        if (emailFromClaim != null) {
            return emailFromClaim;
        }
        return claims.getSubject();
    }

}
