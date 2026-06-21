package com.lolcompanion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Агрегированная статистика по чемпиону для призывателя (Foundation).
 */
@Entity
@Table(
    name = "champion_stats",
    indexes = {
        @Index(name = "idx_champion_stats_summoner_id", columnList = "summoner_id"),
        @Index(name = "idx_champion_stats_champion_name", columnList = "champion_name")
    },
    uniqueConstraints =
        @UniqueConstraint(name = "uk_summoner_champion", columnNames = {"summoner_id", "champion_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "summoner")
public class ChampionStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "summoner_id", nullable = false)
  private Summoner summoner;

  @Column(name = "champion_name", nullable = false, length = 50)
  private String championName;

  @Column(name = "total_games", nullable = false)
  private Integer totalGames = 0;

  @Column(name = "total_wins", nullable = false)
  private Integer totalWins = 0;

  @Column(name = "total_losses", nullable = false)
  private Integer totalLosses = 0;

  @Column(name = "avg_kills", precision = 5, scale = 2)
  private BigDecimal avgKills = BigDecimal.ZERO;

  @Column(name = "avg_deaths", precision = 5, scale = 2)
  private BigDecimal avgDeaths = BigDecimal.ZERO;

  @Column(name = "avg_assists", precision = 5, scale = 2)
  private BigDecimal avgAssists = BigDecimal.ZERO;

  @Column(name = "avg_cs", precision = 6, scale = 2)
  private BigDecimal avgCS = BigDecimal.ZERO;

  @Column(name = "last_played")
  private LocalDateTime lastPlayed;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public double getWinRate() {
    return totalGames == 0 ? 0.0 : (double) totalWins / totalGames * 100.0;
  }

  public double getAverageKDA() {
    double kills = avgKills != null ? avgKills.doubleValue() : 0.0;
    double assists = avgAssists != null ? avgAssists.doubleValue() : 0.0;
    double deaths = avgDeaths != null ? avgDeaths.doubleValue() : 0.0;
    double deathsSafe = deaths == 0 ? 1.0 : deaths;
    return (kills + assists) / deathsSafe;
  }

  public void recordGame(boolean won, int kills, int deaths, int assists, double cs) {
    totalGames++;
    if (won) {
      totalWins++;
    } else {
      totalLosses++;
    }
    int prev = totalGames - 1;
    avgKills = rollingAvg(avgKills, kills, prev);
    avgDeaths = rollingAvg(avgDeaths, deaths, prev);
    avgAssists = rollingAvg(avgAssists, assists, prev);
    avgCS = rollingAvg(avgCS, cs, prev);
    lastPlayed = LocalDateTime.now();
    updatedAt = lastPlayed;
  }

  public String getRecordString() {
    return totalWins + "-" + totalLosses;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  private static BigDecimal rollingAvg(BigDecimal current, double value, int previousCount) {
    BigDecimal base = current != null ? current : BigDecimal.ZERO;
    double next = (base.doubleValue() * previousCount + value) / (previousCount + 1);
    return BigDecimal.valueOf(next).setScale(2, RoundingMode.HALF_UP);
  }
}
