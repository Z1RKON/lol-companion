package com.lolcompanion.service;

/** Человекочитаемые названия режимов LoL (в т.ч. внутренние коды Riot API). */
public final class GameModeLabels {

  private GameModeLabels() {}

  public static String fromRiotInfo(com.lolcompanion.dto.RiotMatchDto.InfoDto info) {
    if (info == null) {
      return "UNKNOWN";
    }
    Integer queueId = info.getQueueId();
    if (queueId != null) {
      String queueLabel = mapQueueId(queueId);
      if (queueLabel != null) {
        return queueLabel;
      }
    }
    return display(info.getGameMode());
  }

  public static String display(String storedMode) {
    if (storedMode == null || storedMode.isBlank()) {
      return "UNKNOWN";
    }
    return switch (storedMode.toUpperCase()) {
      case "CHERRY" -> "Arena";
      case "CLASSIC" -> "Summoner's Rift";
      case "ARAM" -> "ARAM";
      case "URF" -> "URF";
      case "ONEFORALL" -> "One for All";
      case "NEXUSBLITZ" -> "Nexus Blitz";
      case "PRACTICETOOL" -> "Practice Tool";
      case "TUTORIAL" -> "Tutorial";
      default -> storedMode;
    };
  }

  private static String mapQueueId(int queueId) {
    return switch (queueId) {
      case 420 -> "Ranked Solo/Duo";
      case 440 -> "Ranked Flex";
      case 400 -> "Normal Draft";
      case 430 -> "Normal Blind";
      case 450 -> "ARAM";
      case 1700 -> "Arena";
      case 900 -> "URF";
      case 1020 -> "One for All";
      case 1300 -> "Nexus Blitz";
      case 1400 -> "Ultimate Spellbook";
      case 1900 -> "Pick URF";
      default -> null;
    };
  }
}
