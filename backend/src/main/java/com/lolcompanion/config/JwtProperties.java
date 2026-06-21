package com.lolcompanion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {

  private String secret = "change-me-in-production-min-32-chars-long!!";
  private long expirationMs = 86_400_000L;
}
