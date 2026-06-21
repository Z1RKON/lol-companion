package com.lolcompanion.repository;

import com.lolcompanion.entity.Match;
import com.lolcompanion.entity.Match.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Foundation: доступ к {@link Match}. */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

  Optional<Match> findByMatchId(String matchId);

  @EntityGraph(attributePaths = "participantStats")
  Optional<Match> findWithParticipantsByMatchId(String matchId);

  boolean existsByMatchId(String matchId);

  List<Match> findByGameModeOrderByGameCreationTimestampDesc(String gameMode);

  List<Match> findByRegionOrderByGameCreationTimestampDesc(String region);

  List<Match> findByWinningTeamOrderByGameCreationTimestampDesc(Team winningTeam);

  @Query(
      value = "SELECT * FROM matches ORDER BY game_creation_timestamp DESC LIMIT :limit",
      nativeQuery = true)
  List<Match> findRecentMatches(@Param("limit") int limit);

  List<Match> findByGameDurationSecondsLessThanOrderByGameCreationTimestampDesc(int maxSeconds);

  @Query("SELECT COUNT(m) FROM Match m WHERE m.gameMode = :mode AND m.region = :region")
  long countByGameModeAndRegion(@Param("mode") String gameMode, @Param("region") String region);

  void deleteByMatchId(String matchId);
}
