package com.lolcompanion.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация HTTP-клиента Riot Games API (слой Foundation / инфраструктура Mediator).
 *
 * <p>RestTemplate с интерцептором, автоматически добавляющим {@code X-Riot-Token} ко всем запросам.
 */
@Slf4j
@Configuration
public class RiotApiConfig {

  @Bean(name = "riotRestTemplate")
  public RestTemplate riotRestTemplate(
      RestTemplateBuilder builder, RiotApiProperties properties) {

    log.info(
        "RestTemplate для Riot API: regional={}, platform={}",
        properties.getRegionalUrl(),
        properties.getPlatformUrl());

    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(properties.getConnectTimeout());
    factory.setReadTimeout(properties.getReadTimeout());

    RestTemplate restTemplate =
        builder
            .requestFactory(() -> new BufferingClientHttpRequestFactory(factory))
            .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .setReadTimeout(Duration.ofMillis(properties.getReadTimeout()))
            .build();

    restTemplate
        .getInterceptors()
        .add(
            (request, body, execution) -> {
              request.getHeaders().set("X-Riot-Token", properties.getKey());
              request.getHeaders().set("User-Agent", "LoL-Companion/1.0");
              log.debug("Riot HTTP {} {}", request.getMethod(), request.getURI());
              return execution.execute(request, body);
            });

    return restTemplate;
  }
}
