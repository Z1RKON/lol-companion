# Стратегия ORM (Entity → таблицы)

## Data Mapper (Hibernate)

| Entity | Таблица | Ключевые поля |
|--------|---------|---------------|
| User | users | username UK, email UK, passwordHash |
| Summoner | summoners | riotPuuid UK, summonerName, tier, rank, leaguePoints |
| Match | matches | matchId UK, gameMode, gameDuration |
| ParticipantStats | participant_stats | match_id FK, summoner_id FK |
| UserFavoriteSummoner | user_favorite_summoners | user_id + summoner_id UK |

## Связи JPA

- `@ManyToOne(LAZY)` для связей Summoner ↔ UserFavoriteSummoner
- `@OneToMany` Match ↔ ParticipantStats
- Каскады: сохранение матча и участников в одной транзакции (`MatchService`)

## Identity Map

Кэш первого уровня Hibernate — один объект на транзакцию.

## Lazy Load

Связанные сущности не загружаются до обращения (`FetchType.LAZY`).

Детали Foundation: [../03-architecture/foundation-layer.md](../03-architecture/foundation-layer.md)
