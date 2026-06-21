package com.lolcompanion.repository;

import com.lolcompanion.entity.ChampionStats;
import com.lolcompanion.entity.Summoner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Foundation: агрегаты по чемпионам. */
@Repository
public interface ChampionStatsRepository extends JpaRepository<ChampionStats, Long> {

  Optional<ChampionStats> findBySummonerAndChampionName(Summoner summoner, String championName);

  List<ChampionStats> findBySummonerOrderByTotalGamesDesc(Summoner summoner);

  List<ChampionStats> findByChampionNameOrderByTotalGamesDesc(String championName);
}
