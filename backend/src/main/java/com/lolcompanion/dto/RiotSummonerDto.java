package com.lolcompanion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO для ответа Riot API при запросе профиля запросов /lol/summoner/v4/by-name/{summonerName}
 * Содержит базовую информацию о призывателе.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiotSummonerDto {
    
    @JsonProperty("id")
    private String summonerId;
    
    @JsonProperty("accountId")
    private String accountId;
    
    @JsonProperty("puuid")
    private String puuid;
    
    @JsonProperty("name")
    private String summonerName;
    
    @JsonProperty("profileIconId")
    private Integer profileIconId;
    
    @JsonProperty("summonerLevel")
    private Integer summonerLevel;
    
    @JsonProperty("revisionDate")
    private Long revisionDate;
}
