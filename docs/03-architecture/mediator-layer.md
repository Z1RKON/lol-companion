# Слой Mediator (бизнес-логика)

## Компоненты

| Класс | Назначение |
|-------|------------|
| `RiotApiConfig` | `RestTemplate` + интерцептор `X-Riot-Token` |
| `RiotApiClient` | HTTP к Riot API (summoner, league, match ids, match details) |
| `SummonerService` | Cache-Aside TTL 10 мин → `SummonerResponseDto` |
| `MatchService` | История матчей → кэш `Match` / `ParticipantStats` → `MatchHistoryItemDto` |

## Поток данных (PCMEF)

```
Controller → SummonerService / MatchService → RiotApiClient + Repository → PostgreSQL
                    ↓
              API DTO (без Entity наружу)
```

## Riot эндпоинты

- `GET /lol/summoner/v4/summoners/by-name/{name}` (fallback: `/lol/summoner/v4/by-name/{name}`)
- `GET /lol/league/v4/entries/by-summoner/{summonerId}`
- `GET /lol/match/v5/matches/by-puuid/{puuid}/ids`
- `GET /lol/match/v5/matches/{matchId}`

## Кэширование

- **Профиль:** PostgreSQL, TTL из `riot.api.cache-ttl-minutes` (по умолчанию 10).
- **Матчи:** после первого сохранения только чтение из БД (матч неизменяем).
