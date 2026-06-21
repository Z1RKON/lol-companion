package com.lolcompanion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * Матч League of Legends (слой Entity / Foundation).
 */
@Entity
@Table(
    name = "matches",
    indexes = {
        @Index(name = "idx_matches_match_id", columnList = "match_id"),
        @Index(name = "idx_matches_game_creation", columnList = "game_creation_timestamp"),
        @Index(name = "idx_matches_game_mode", columnList = "game_mode")
    },
    uniqueConstraints = @UniqueConstraint(name = "uk_matches_match_id", columnNames = "match_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "participantStats")
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "match_id", nullable = false, unique = true, length = 50)
  private String matchId;

  @Column(name = "game_mode", nullable = false, length = 30)
  private String gameMode;

  @Column(name = "game_duration_seconds", nullable = false)
  private Integer gameDurationSeconds;

  @Column(name = "game_creation_timestamp", nullable = false)
  private Long gameCreationTimestamp;

  @Column(name = "game_start_timestamp")
  private Long gameStartTimestamp;

  @Column(name = "game_end_timestamp")
  private Long gameEndTimestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "winning_team", nullable = false, length = 10)
  private Team winningTeam;

  @Column(nullable = false, length = 10)
  private String region = "EUW1";

  @Column(name = "patch_version", length = 20)
  private String patchVersion;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ParticipantStats> participantStats = new HashSet<>();

  // ——— Бизнес-логика ———

  public static Match fromRiotSummary(
      String matchId,
      String gameMode,
      int durationSeconds,
      long gameCreationEpochMs,
      Long gameStartEpochMs,
      Team winningTeam,
      String region,
      String patchVersion) {
    Match match = new Match();
    match.matchId = matchId;
    match.gameMode = gameMode;
    match.gameDurationSeconds = Math.max(1, durationSeconds);
    match.gameCreationTimestamp = gameCreationEpochMs;
    match.gameStartTimestamp = gameStartEpochMs;
    if (gameStartEpochMs != null) {
      match.gameEndTimestamp = gameStartEpochMs + durationSeconds * 1000L;
    }
    match.winningTeam = winningTeam;
    match.region = region != null ? region : "EUW1";
    match.patchVersion = patchVersion;
    return match;
  }

  public void addParticipant(ParticipantStats stats) {
    if (stats == null) {
      return;
    }
    this.participantStats.add(stats);
    stats.assignMatch(this);
  }

  public boolean isComplete() {
    return participantStats.size() == 10;
  }

  public boolean isFinished() {
    return gameEndTimestamp != null
        && gameStartTimestamp != null
        && gameEndTimestamp >= gameStartTimestamp;
  }

  public int getGameDurationMinutes() {
    return (int) Math.ceil(gameDurationSeconds / 60.0);
  }

  public LocalDateTime getGameCreationDateTime() {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(gameCreationTimestamp), ZoneId.systemDefault());
  }

  public Set<ParticipantStats> getWinners() {
    Set<ParticipantStats> winners = new HashSet<>();
    for (ParticipantStats ps : participantStats) {
      if (ps.isWin()) {
        winners.add(ps);
      }
    }
    return winners;
  }

  public Set<ParticipantStats> getParticipantsOfTeam(Team team) {
    Set<ParticipantStats> result = new HashSet<>();
    for (ParticipantStats ps : participantStats) {
      if (ps.getTeam() == team) {
        result.add(ps);
      }
    }
    return result;
  }

  public long getAverageGoldForTeam(Team team) {
    return (long)
        getParticipantsOfTeam(team).stream()
            .mapToLong(ParticipantStats::getGoldEarned)
            .average()
            .orElse(0.0);
  }

  public enum Team {
    BLUE,
    RED;

    public static Team fromRiotTeamId(int teamId) {
      return teamId == 100 ? BLUE : RED;
    }
  }
}
