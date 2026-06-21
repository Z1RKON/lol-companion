package com.lolcompanion.config;

/** Маппинг региона LoL → URL Riot API (regional + platform routing). */
public enum RiotRegion {
  RU("RU", "https://ru.api.riotgames.com", "https://europe.api.riotgames.com", "RU"),
  EUW("EUW", "https://euw1.api.riotgames.com", "https://europe.api.riotgames.com", "EUW1"),
  EUNE("EUNE", "https://eun1.api.riotgames.com", "https://europe.api.riotgames.com", "EUN1"),
  NA("NA", "https://na1.api.riotgames.com", "https://americas.api.riotgames.com", "NA1"),
  KR("KR", "https://kr.api.riotgames.com", "https://asia.api.riotgames.com", "KR"),
  BR("BR", "https://br1.api.riotgames.com", "https://americas.api.riotgames.com", "BR1"),
  LAN("LAN", "https://la1.api.riotgames.com", "https://americas.api.riotgames.com", "LA1"),
  LAS("LAS", "https://la2.api.riotgames.com", "https://americas.api.riotgames.com", "LA2"),
  OCE("OCE", "https://oc1.api.riotgames.com", "https://sea.api.riotgames.com", "OC1"),
  TR("TR", "https://tr1.api.riotgames.com", "https://europe.api.riotgames.com", "TR1"),
  JP("JP", "https://jp1.api.riotgames.com", "https://asia.api.riotgames.com", "JP1");

  private final String code;
  private final String regionalUrl;
  private final String platformUrl;
  private final String storageCode;

  RiotRegion(String code, String regionalUrl, String platformUrl, String storageCode) {
    this.code = code;
    this.regionalUrl = regionalUrl;
    this.platformUrl = platformUrl;
    this.storageCode = storageCode;
  }

  public String getCode() {
    return code;
  }

  public String getRegionalUrl() {
    return regionalUrl;
  }

  public String getPlatformUrl() {
    return platformUrl;
  }

  public String getStorageCode() {
    return storageCode;
  }

  public static RiotRegion fromCode(String raw) {
    if (raw == null || raw.isBlank()) {
      return RU;
    }
    String normalized = raw.trim().toUpperCase();
    for (RiotRegion region : values()) {
      if (region.code.equals(normalized) || region.storageCode.equals(normalized)) {
        return region;
      }
    }
    throw new IllegalArgumentException("Неизвестный регион: " + raw);
  }
}
