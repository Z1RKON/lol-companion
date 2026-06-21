package com.lolcompanion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Призыватель LoL — кэш данных Riot API (слой Entity / Foundation).
 */
@Entity
@Table(
    name = "summoners",
    indexes = {
        @Index(name = "idx_summoners_riot_puuid", columnList = "riot_puuid"),
        @Index(name = "idx_summoners_summoner_name", columnList = "summoner_name"),
        @Index(name = "idx_summoners_last_updated", columnList = "last_updated")
    },
    uniqueConstraints = @UniqueConstraint(name = "uk_summoners_riot_puuid", columnNames = "riot_puuid")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"participantStats", "favoriteUsers", "championStats"})
public class Summoner {

  /** TTL кэша профиля (минуты), согласован с riot.api.cache-ttl-minutes. */
  public static final int CACHE_TTL_MINUTES = 10;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "riot_puuid", nullable = false, unique = true, length = 78)
  private String puuid;

  @Column(name = "summoner_name", nullable = false, length = 50)
  private String summonerName;

  @Column(name = "summoner_level", nullable = false)
  private Integer summonerLevel = 1;

  @Column(name = "profile_icon_id")
  private Integer profileIconId;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Tier tier;

  @Enumerated(EnumType.STRING)
  @Column(length = 5)
  private Rank rank;

  @Column(name = "league_points", nullable = false)
  private Integer leaguePoints = 0;

  @Column(name = "win_count", nullable = false)
  private Integer winCount = 0;

  @Column(name = "loss_count", nullable = false)
  private Integer lossCount = 0;

  @Column(nullable = false, length = 10)
  private String region = "EUW1";

  @Column(name = "last_updated", nullable = false)
  private LocalDateTime lastUpdated = LocalDateTime.now();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "summoner", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ParticipantStats> participantStats = new HashSet<>();

  @OneToMany(mappedBy = "summoner", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserFavoriteSummoner> favoriteUsers = new HashSet<>();

  @OneToMany(mappedBy = "summoner", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ChampionStats> championStats = new HashSet<>();

  // ——— Бизнес-логика ———

  public void updateFromRiot(
      String summonerName, int level, Integer profileIconId, String region) {
    if (summonerName == null || summonerName.isBlank()) {
      throw new IllegalArgumentException("Имя призывателя обязательно");
    }
    this.summonerName = summonerName;
    this.summonerLevel = Math.max(1, Math.min(level, 9999));
    this.profileIconId = profileIconId;
    if (region != null && !region.isBlank()) {
      this.region = region;
    }
    markRefreshed();
  }

  public void updateRank(Tier newTier, Rank newRank, int leaguePoints) {
    this.tier = newTier;
    this.rank = newRank;
    this.leaguePoints = Math.max(0, leaguePoints);
    markRefreshed();
  }

  /** Сброс ранговой статистики (игрок без ranked-очереди в League API). */
  public void clearRankedStats() {
    this.tier = null;
    this.rank = null;
    this.leaguePoints = 0;
    this.winCount = 0;
    this.lossCount = 0;
    markRefreshed();
  }

  public void updateRankedStats(
      Tier newTier, Rank newRank, int leaguePoints, int wins, int losses) {
    this.tier = newTier;
    this.rank = newRank;
    this.leaguePoints = Math.max(0, leaguePoints);
    this.winCount = Math.max(0, wins);
    this.lossCount = Math.max(0, losses);
    markRefreshed();
  }

  public void recordRankedOutcome(boolean won) {
    if (won) {
      this.winCount++;
    } else {
      this.lossCount++;
    }
    markRefreshed();
  }

  public double getWinRate() {
    int total = this.winCount + this.lossCount;
    return total == 0 ? 0.0 : (double) this.winCount / total * 100.0;
  }

  public String getFullRankLabel() {
    if (tier == null) {
      return "UNRANKED";
    }
    return rank != null ? tier.name() + " " + rank.name() : tier.name();
  }

  public boolean isRanked() {
    return tier != null;
  }

  public boolean isCacheFresh() {
    return isCacheFresh(CACHE_TTL_MINUTES);
  }

  public boolean isCacheFresh(int ttlMinutes) {
    return lastUpdated != null
        && lastUpdated.isAfter(LocalDateTime.now().minusMinutes(ttlMinutes));
  }

  public void markRefreshed() {
    this.lastUpdated = LocalDateTime.now();
  }

  public void addParticipantStats(ParticipantStats stats) {
    if (stats == null) {
      return;
    }
    this.participantStats.add(stats);
    stats.assignSummoner(this);
  }

  public enum Tier {
    IRON,
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    EMERALD,
    DIAMOND,
    MASTER,
    GRANDMASTER,
    CHALLENGER
  }

  public enum Rank {
    I,
    II,
    III,
    IV
  }
}
