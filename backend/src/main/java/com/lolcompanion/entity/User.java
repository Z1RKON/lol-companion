package com.lolcompanion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Локальный пользователь приложения-компаньона (слой Entity / Foundation).
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"passwordHash", "favoriteSummoners"})
public class User {

  private static final int USERNAME_MIN_LENGTH = 3;
  private static final int USERNAME_MAX_LENGTH = 50;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role = UserRole.USER;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "linked_riot_puuid", length = 78)
  private String linkedRiotPuuid;

  @Column(name = "linked_riot_id", length = 50)
  private String linkedRiotId;

  @Column(name = "linked_riot_region", length = 10)
  private String linkedRiotRegion;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserFavoriteSummoner> favoriteSummoners = new HashSet<>();

  // ——— Бизнес-логика ———

  public static User register(String username, String email, String passwordHash) {
    User user = new User();
    user.applyCredentials(username, email, passwordHash);
    user.role = UserRole.USER;
    user.createdAt = LocalDateTime.now();
    user.updatedAt = user.createdAt;
    return user;
  }

  public void applyCredentials(String username, String email, String passwordHash) {
    validateUsername(username);
    if (email == null || !email.contains("@")) {
      throw new IllegalArgumentException("Некорректный email");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Хеш пароля не может быть пустым");
    }
    this.username = username.trim();
    this.email = email.trim().toLowerCase();
    this.passwordHash = passwordHash;
    touch();
  }

  public void changePasswordHash(String newPasswordHash) {
    if (newPasswordHash == null || newPasswordHash.isBlank()) {
      throw new IllegalArgumentException("Новый пароль не задан");
    }
    this.passwordHash = newPasswordHash;
    touch();
  }

  public void promoteTo(UserRole newRole) {
    if (newRole == null) {
      throw new IllegalArgumentException("Роль не указана");
    }
    this.role = newRole;
    touch();
  }

  public boolean isAdmin() {
    return this.role == UserRole.ADMIN;
  }

  public boolean canModerate() {
    return this.role == UserRole.ADMIN || this.role == UserRole.MODERATOR;
  }

  public UserFavoriteSummoner addFavoriteSummoner(Summoner summoner) {
    if (summoner == null || summoner.getId() == null) {
      throw new IllegalArgumentException("Призыватель должен быть сохранён в БД");
    }
    if (isSummonerInFavorites(summoner)) {
      throw new IllegalStateException("Призыватель уже в избранном");
    }
    UserFavoriteSummoner link = UserFavoriteSummoner.create(this, summoner);
    this.favoriteSummoners.add(link);
    return link;
  }

  public void removeFavoriteSummoner(Summoner summoner) {
    this.favoriteSummoners.removeIf(
        fav ->
            fav.getSummoner() != null
                && summoner.getId() != null
                && fav.getSummoner().getId().equals(summoner.getId()));
  }

  public boolean isSummonerInFavorites(Summoner summoner) {
    if (summoner == null || summoner.getId() == null) {
      return false;
    }
    return this.favoriteSummoners.stream()
        .anyMatch(fav -> summoner.getId().equals(fav.getSummoner().getId()));
  }

  public int getFavoriteSummonersCount() {
    return this.favoriteSummoners.size();
  }

  public boolean hasLinkedRiotAccount() {
    return linkedRiotPuuid != null
        && !linkedRiotPuuid.isBlank()
        && linkedRiotId != null
        && !linkedRiotId.isBlank();
  }

  public void linkRiotAccount(String puuid, String riotId, String region) {
    if (puuid == null || puuid.isBlank()) {
      throw new IllegalArgumentException("PUUID обязателен");
    }
    if (riotId == null || riotId.isBlank()) {
      throw new IllegalArgumentException("Riot ID обязателен");
    }
    if (region == null || region.isBlank()) {
      throw new IllegalArgumentException("Регион обязателен");
    }
    this.linkedRiotPuuid = puuid;
    this.linkedRiotId = riotId.trim();
    this.linkedRiotRegion = region.trim().toUpperCase();
    touch();
  }

  public void unlinkRiotAccount() {
    this.linkedRiotPuuid = null;
    this.linkedRiotId = null;
    this.linkedRiotRegion = null;
    touch();
  }

  @PreUpdate
  public void onUpdate() {
    touch();
  }

  private void touch() {
    this.updatedAt = LocalDateTime.now();
  }

  private static void validateUsername(String username) {
    if (username == null) {
      throw new IllegalArgumentException("Username обязателен");
    }
    String trimmed = username.trim();
    if (trimmed.length() < USERNAME_MIN_LENGTH || trimmed.length() > USERNAME_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Username: от " + USERNAME_MIN_LENGTH + " до " + USERNAME_MAX_LENGTH + " символов");
    }
  }

  public enum UserRole {
    USER,
    ADMIN,
    MODERATOR
  }
}
