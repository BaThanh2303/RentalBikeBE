package com.example.rentalebike.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String secret = "mysuperlongsecretkey1234567890abcdef"; // TODO: move to config
    private final long expirationMs = 86400000; // 1 ngày

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Date expiration = Jwts.parser().setSigningKey(secret.getBytes())
                .parseClaimsJws(token).getBody().getExpiration();
        return expiration.before(new Date());
    }
}
