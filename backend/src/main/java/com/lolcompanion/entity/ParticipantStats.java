package com.lolcompanion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Статистика одного призывателя в одном матче (слой Entity / Foundation).
 */
@Entity
@Table(
    name = "participant_stats",
    indexes = {
        @Index(name = "idx_participant_stats_match_id", columnList = "match_id"),
        @Index(name = "idx_participant_stats_summoner_id", columnList = "summoner_id"),
        @Index(name = "idx_participant_stats_riot_puuid", columnList = "riot_puuid"),
        @Index(name = "idx_participant_stats_champion_name", columnList = "champion_name")
    },
    uniqueConstraints =
        @UniqueConstraint(name = "uk_participant_match_puuid", columnNames = {"match_id", "riot_puuid"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"match", "summoner"})
public class ParticipantStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "match_id", nullable = false)
  private Match match;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "summoner_id")
  private Summoner summoner;

  @Column(name = "riot_puuid", nullable = false, length = 78)
  private String riotPuuid;

  @Column(name = "champion_name", nullable = false, length = 50)
  private String championName;

  @Column(name = "summoner_display_name", length = 50)
  private String summonerDisplayName;

  @Column(name = "profile_icon_id")
  private Integer profileIconId;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Role role = Role.UNKNOWN;

  @Column(nullable = false)
  private Integer kills = 0;

  @Column(nullable = false)
  private Integer deaths = 0;

  @Column(nullable = false)
  private Integer assists = 0;

  @Column(name = "cs_score", nullable = false)
  private BigDecimal csScore = BigDecimal.ZERO;

  @Column(name = "gold_earned", nullable = false)
  private Long goldEarned = 0L;

  @Column(name = "damage_dealt", nullable = false)
  private Long damageDealt = 0L;

  @Column(name = "damage_to_champions", nullable = false)
  private Long damageToChampions = 0L;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Match.Team team;

  @Column(nullable = false)
  private boolean win;

  @Column(name = "item0", nullable = false)
  private Integer item0 = 0;

  @Column(name = "item1", nullable = false)
  private Integer item1 = 0;

  @Column(name = "item2", nullable = false)
  private Integer item2 = 0;

  @Column(name = "item3", nullable = false)
  private Integer item3 = 0;

  @Column(name = "item4", nullable = false)
  private Integer item4 = 0;

  @Column(name = "item5", nullable = false)
  private Integer item5 = 0;

  @Column(name = "item6", nullable = false)
  private Integer item6 = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  // ——— Бизнес-логика ———

  public static ParticipantStats create(
      Match match,
      Summoner summoner,
      String riotPuuid,
      String summonerDisplayName,
      String championName,
      Role role,
      Match.Team team,
      boolean win,
      int kills,
      int deaths,
      int assists,
      double cs,
      long goldEarned,
      long damageDealt,
      long damageToChampions,
      Integer profileIconId,
      int... items) {
    ParticipantStats stats = new ParticipantStats();
    stats.assignMatch(match);
    stats.assignSummoner(summoner);
    stats.riotPuuid = riotPuuid;
    stats.summonerDisplayName = summonerDisplayName;
    stats.championName = championName;
    stats.profileIconId = profileIconId;
    stats.role = role != null ? role : Role.UNKNOWN;
    stats.team = team;
    stats.win = win;
    stats.applyCombatStats(kills, deaths, assists, cs, goldEarned, damageDealt, damageToChampions);
    stats.applyItems(items);
    return stats;
  }

  public void applyItems(int... items) {
    if (items == null || items.length == 0) {
      return;
    }
    this.item0 = itemAt(items, 0);
    this.item1 = itemAt(items, 1);
    this.item2 = itemAt(items, 2);
    this.item3 = itemAt(items, 3);
    this.item4 = itemAt(items, 4);
    this.item5 = itemAt(items, 5);
    this.item6 = itemAt(items, 6);
  }

  public java.util.List<Integer> getItemIds() {
    return java.util.List.of(item0, item1, item2, item3, item4, item5, item6);
  }

  private static int itemAt(int[] items, int index) {
    if (index >= items.length) {
      return 0;
    }
    return Math.max(0, items[index]);
  }

  public void assignMatch(Match match) {
    this.match = match;
  }

  public void assignSummoner(Summoner summoner) {
    this.summoner = summoner;
    if (summoner != null && summoner.getPuuid() != null) {
      this.riotPuuid = summoner.getPuuid();
    }
  }

  public void applyCombatStats(
      int kills,
      int deaths,
      int assists,
      double cs,
      long goldEarned,
      long damageDealt,
      long damageToChampions) {
    this.kills = Math.max(0, kills);
    this.deaths = Math.max(0, deaths);
    this.assists = Math.max(0, assists);
    this.csScore = BigDecimal.valueOf(Math.max(0, cs)).setScale(2, RoundingMode.HALF_UP);
    this.goldEarned = Math.max(0, goldEarned);
    this.damageDealt = Math.max(0, damageDealt);
    this.damageToChampions = Math.max(0, damageToChampions);
  }

  public double calculateKda() {
    int deathDivisor = Math.max(deaths, 1);
    return (double) (kills + assists) / deathDivisor;
  }

  public String formatKda() {
    return kills + " / " + deaths + " / " + assists;
  }

  public boolean isPerfectGame() {
    return deaths == 0 && kills + assists > 0;
  }

  public boolean isMvpCandidate() {
    return win && calculateKda() >= 5.0;
  }

  public enum Role {
    TOP,
    JUNGLE,
    MIDDLE,
    ADC,
    SUPPORT,
    UNKNOWN
  }
}
