package com.lolcompanion.repository;

import com.lolcompanion.entity.Match;
import com.lolcompanion.entity.ParticipantStats;
import com.lolcompanion.entity.ParticipantStats.Role;
import com.lolcompanion.entity.Summoner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Foundation: доступ к {@link ParticipantStats}. */
@Repository
public interface ParticipantStatsRepository extends JpaRepository<ParticipantStats, Long> {

  List<ParticipantStats> findByMatch(Match match);

  List<ParticipantStats> findBySummoner(Summoner summoner);

  Optional<ParticipantStats> findByMatchAndRiotPuuid(Match match, String riotPuuid);

  List<ParticipantStats> findByRiotPuuidOrderByCreatedAtDesc(String riotPuuid);

  List<ParticipantStats> findBySummonerAndChampionNameOrderByCreatedAtDesc(
      Summoner summoner, String championName);

  List<ParticipantStats> findBySummonerAndRoleOrderByCreatedAtDesc(Summoner summoner, Role role);

  @Query(
      "SELECT COUNT(ps) FROM ParticipantStats ps WHERE ps.summoner = :summoner AND ps.win = true")
  long countWinsBySummoner(@Param("summoner") Summoner summoner);

  @Query(
      "SELECT COUNT(ps) FROM ParticipantStats ps WHERE ps.summoner = :summoner AND ps.win = false")
  long countLossesBySummoner(@Param("summoner") Summoner summoner);

  @Query(
      value =
          "SELECT * FROM participant_stats WHERE summoner_id = :summonerId ORDER BY created_at DESC LIMIT :limit",
      nativeQuery = true)
  List<ParticipantStats> findRecentBySummonerId(
      @Param("summonerId") Long summonerId, @Param("limit") int limit);

  @Query(
      value =
          "SELECT champion_name, COUNT(*) AS games, SUM(CASE WHEN win THEN 1 ELSE 0 END) AS wins "
              + "FROM participant_stats WHERE summoner_id = :summonerId "
              + "GROUP BY champion_name ORDER BY games DESC LIMIT :limit",
      nativeQuery = true)
  List<Object[]> findTopChampionsBySummonerId(
      @Param("summonerId") Long summonerId, @Param("limit") int limit);
}
