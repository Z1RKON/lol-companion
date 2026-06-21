package com.lolcompanion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Ответ Account-V1: Riot ID → PUUID. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotAccountDto {

  @JsonProperty("puuid")
  private String puuid;

  @JsonProperty("gameName")
  private String gameName;

  @JsonProperty("tagLine")
  private String tagLine;
}
