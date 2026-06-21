package com.lolcompanion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Связь пользователь — избранный призыватель (Foundation).
 */
@Entity
@Table(
    name = "user_favorite_summoners",
    indexes = {
        @Index(name = "idx_user_fav_summoners_user_id", columnList = "user_id"),
        @Index(name = "idx_user_fav_summoners_summoner_id", columnList = "summoner_id")
    },
    uniqueConstraints = @UniqueConstraint(name = "uk_user_summoner", columnNames = {"user_id", "summoner_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "summoner"})
public class UserFavoriteSummoner {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "summoner_id", nullable = false)
  private Summoner summoner;

  @Column(name = "added_at", nullable = false, updatable = false)
  private LocalDateTime addedAt = LocalDateTime.now();

  public static UserFavoriteSummoner create(User user, Summoner summoner) {
    UserFavoriteSummoner link = new UserFavoriteSummoner();
    link.user = user;
    link.summoner = summoner;
    link.addedAt = LocalDateTime.now();
    return link;
  }

  public long getDaysSinceAdded() {
    return java.time.temporal.ChronoUnit.DAYS.between(addedAt, LocalDateTime.now());
  }

  public boolean isRecentlyAdded() {
    return getDaysSinceAdded() < 7;
  }
}
