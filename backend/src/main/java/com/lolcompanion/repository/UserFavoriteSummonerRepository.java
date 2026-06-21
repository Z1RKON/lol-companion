package com.lolcompanion.repository;

import com.lolcompanion.entity.Summoner;
import com.lolcompanion.entity.User;
import com.lolcompanion.entity.UserFavoriteSummoner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Foundation: избранные призыватели пользователя. */
@Repository
public interface UserFavoriteSummonerRepository extends JpaRepository<UserFavoriteSummoner, Long> {

  List<UserFavoriteSummoner> findByUserOrderByAddedAtDesc(User user);

  @Query(
      "SELECT f FROM UserFavoriteSummoner f JOIN FETCH f.summoner WHERE f.user = :user ORDER BY f.addedAt DESC")
  List<UserFavoriteSummoner> findByUserWithSummoner(@Param("user") User user);

  Optional<UserFavoriteSummoner> findByUserAndSummoner(User user, Summoner summoner);

  boolean existsByUserAndSummoner(User user, Summoner summoner);

  long countByUser(User user);

  void deleteByUserAndSummoner(User user, Summoner summoner);
}
