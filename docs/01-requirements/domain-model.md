# Domain Model (концептуальная модель классов)

![Domain model](../images/fig-03-domain.png)

Рисунок 3 — Доменная модель

## Сущности

| Сущность | Ключевые атрибуты | Связи |
|----------|-------------------|-------|
| User | username, email, linkedRiotPuuid | M:N Summoner (избранное) |
| Summoner | riotPuuid, summonerName, tier, rank, LP | M:N User; 1:N ParticipantStats |
| Match | matchId, gameMode, gameDuration | 1:N ParticipantStats |
| ParticipantStats | kills, deaths, assists, championName, win | N:1 Match, N:1 Summoner |
| UserFavoriteSummoner | addedAt | N:1 User, N:1 Summoner |

## Бизнес-методы (Entity)

- `Summoner.getWinRate()`, `Summoner.isCacheFresh()`
- `User.linkRiotAccount()`, `User.addFavoriteSummoner()`
