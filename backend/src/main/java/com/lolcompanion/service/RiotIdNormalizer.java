package com.lolcompanion.service;

/** Нормализация Riot ID перед запросами к API и сохранением в БД. */
public final class RiotIdNormalizer {

  private RiotIdNormalizer() {}

  public static String normalizeQuery(String raw) {
    if (raw == null) {
      return "";
    }
    // Убираем невидимые символы форматирования (⁦ ⁩ и т.п.) и лишние пробелы.
    return raw.replaceAll("\\p{Cf}", "").trim();
  }

  public static ParsedRiotId parseRiotId(String raw) {
    String normalized = normalizeQuery(raw);
    int hashIndex = normalized.indexOf('#');
    if (hashIndex < 0) {
      return new ParsedRiotId(normalized, null, normalized);
    }

    String gameName = normalized.substring(0, hashIndex).trim();
    String tagLine = normalized.substring(hashIndex + 1).trim();
    return new ParsedRiotId(gameName, tagLine, normalized);
  }

  public record ParsedRiotId(String gameName, String tagLine, String fullRiotId) {
    public boolean hasTag() {
      return tagLine != null && !tagLine.isBlank();
    }
  }
}
