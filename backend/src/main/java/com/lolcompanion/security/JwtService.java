package com.lolcompanion.security;

import com.lolcompanion.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties jwtProperties;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  public String generateToken(UserPrincipal principal) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

    return Jwts.builder()
        .subject(principal.getUsername())
        .claim("userId", principal.getId())
        .claim("email", principal.getEmail())
        .claim("role", principal.getRole())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey())
        .compact();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public Long extractUserId(String token) {
    return parseClaims(token).get("userId", Long.class);
  }

  public boolean isTokenValid(String token, UserPrincipal principal) {
    Claims claims = parseClaims(token);
    String username = claims.getSubject();
    Date expiration = claims.getExpiration();
    return username.equals(principal.getUsername()) && expiration.after(new Date());
  }

  private SecretKey signingKey() {
    byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
