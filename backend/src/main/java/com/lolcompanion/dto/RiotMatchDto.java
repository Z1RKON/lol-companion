package com.lolcompanion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO Match-V5: /lol/match/v5/matches/{matchId} (JSON в camelCase). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotMatchDto {

  private MetadataDto metadata;
  private InfoDto info;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class MetadataDto {
    private String matchId;
    private List<String> participants;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InfoDto {
    private Long gameCreation;
    private Integer gameDuration;
    private Long gameEndTimestamp;
    private Long gameId;
    private String gameMode;
    private Long gameStartTimestamp;
    private String gameType;
    private String gameVersion;
    private List<ParticipantDto> participants;
    private String platformId;
    private Integer queueId;
    private List<TeamDto> teams;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ParticipantDto {
    private String puuid;
    private Integer championId;
    private String championName;
    private String riotIdGameName;
    private String riotIdTagline;
    private String summonerName;
    private Integer profileIcon;
    private String role;
    private String lane;
    private Integer teamId;
    private String teamPosition;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer totalMinionsKilled;
    private Integer neutralMinionsKilled;
    private Long goldEarned;
    private Long totalDamageDealtToChampions;
    private Long totalDamageTaken;
    private Long totalHeal;
    private Boolean win;
    private Integer item0;
    private Integer item1;
    private Integer item2;
    private Integer item3;
    private Integer item4;
    private Integer item5;
    private Integer item6;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TeamDto {
    private Integer teamId;
    private Boolean win;
    private List<BanDto> bans;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BanDto {
    @JsonProperty("championId")
    private Integer championId;

    @JsonProperty("pickTurn")
    private Integer pickTurn;
  }
}
