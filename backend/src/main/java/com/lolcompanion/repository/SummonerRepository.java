package com.lolcompanion.repository;

import com.lolcompanion.entity.Summoner;
import com.lolcompanion.entity.Summoner.Tier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Foundation: доступ к {@link Summoner}. */
@Repository
public interface SummonerRepository extends JpaRepository<Summoner, Long> {

  Optional<Summoner> findByPuuid(String puuid);

  List<Summoner> findBySummonerNameIgnoreCase(String summonerName);

  @Query(
      "SELECT s FROM Summoner s WHERE LOWER(s.summonerName) = LOWER(:name) AND s.region = :region")
  Optional<Summoner> findBySummonerNameIgnoreCaseAndRegion(
      @Param("name") String summonerName, @Param("region") String region);

  boolean existsByPuuid(String puuid);

  List<Summoner> findByTierOrderByLeaguePointsDesc(Tier tier);

  @Query(
      value =
          "SELECT * FROM summoners WHERE last_updated < NOW() - (:minutes * INTERVAL '1 minute')",
      nativeQuery = true)
  List<Summoner> findStaleCacheOlderThanMinutes(@Param("minutes") int minutes);
}
