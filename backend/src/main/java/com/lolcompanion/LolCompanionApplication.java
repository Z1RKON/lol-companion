package com.lolcompanion;

import com.lolcompanion.config.JwtProperties;
import com.lolcompanion.config.RiotApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RiotApiProperties.class, JwtProperties.class})
public class LolCompanionApplication {

  public static void main(String[] args) {
    SpringApplication.run(LolCompanionApplication.class, args);
  }
}
