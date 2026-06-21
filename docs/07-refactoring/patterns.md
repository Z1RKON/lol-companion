# Рефакторинг и паттерны

## Data Mapper (обязательно)

Hibernate ORM отделяет Entity от схемы PostgreSQL. Entity-классы не содержат SQL.

## Identity Map (обязательно)

Кэш первого уровня Hibernate — повторная загрузка в рамках транзакции без дополнительного SELECT.

## Cache-Aside (профиль призывателя)

`SummonerService`:
1. `findByPuuid` / поиск по имени в БД
2. `Summoner.isCacheFresh()` — TTL 10 мин
3. При устаревании → `RiotApiClient` → save

Матчи: write-once — после сохранения только чтение из БД.

## Паттерны GoF

| Паттерн | Применение |
|---------|------------|
| Facade | `RiotApiClient` — единая точка Riot API |
| Strategy | `RiotRegion` — выбор routing region |
| Template Method | Алгоритм Cache-Aside в SummonerService |

## PUUID-рефакторинг

Внутренняя логика переведена с `summonerName` на `puuid` для избранного и навигации (ADR-004).
