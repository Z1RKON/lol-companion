package com.lolcompanion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметры Riot API из {@code application.yml} (префикс {@code riot.api}).
 *
 * <p>Правило 4: ключ только через {@code riot.api.key: ${RIOT_API_KEY}}.
 */
@ConfigurationProperties(prefix = "riot.api")
@Data
public class RiotApiProperties {

  /** Riot API Key — {@code ${RIOT_API_KEY}}. */
  private String key;

  private String regionalUrl = "https://ru.api.riotgames.com";

  private String platformUrl = "https://europe.api.riotgames.com";

  private String defaultRegion = "EUW1";

  private int connectTimeout = 10_000;

  private int readTimeout = 15_000;

  private int cacheTtlMinutes = 10;

  /** Совместимость со старым полем {@code api-key} в yaml. */
  public String getApiKey() {
    return key;
  }

  public void setApiKey(String apiKey) {
    this.key = apiKey;
  }
}
