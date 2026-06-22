# Слои PCMEF (backend)

## Направление зависимостей

```
Presentation (GlobalExceptionHandler)
    ↑
Control (AuthController, SummonerController)
    ↑
Mediator (AuthService, SummonerService, MatchService, FavoriteService, RiotApiClient)
    ↑
Entity (User, Summoner, Match, ParticipantStats, ...)
    ↑
Foundation (UserRepository, SummonerRepository, MatchRepository, config)
```

## Пакеты `com.lolcompanion`

| Слой | Пакет | Примеры классов |
|------|-------|-----------------|
| Presentation | `advice` | GlobalExceptionHandler |
| Control | `controller` | AuthController, SummonerController |
| Mediator | `service` | SummonerService, RiotApiClient |
| Entity | `entity` | Summoner, User, Match |
| Foundation | `repository`, `config` | SummonerRepository, RiotApiConfig |

## Mobile (Presentation)

| Компонент | Путь |
|-----------|------|
| Экраны | `mobile/src/screens/` |
| Навигация | `mobile/src/navigation/` |
| Состояние | `mobile/src/store/` (Zustand) |
| API | `mobile/src/api/client.ts` |

**Запрещено:** Controller → RiotApiClient; Entity → Repository.

Детали слоёв: [pcmef-layers.md](pcmef-layers.md), [../05-implementation/structure.md](../05-implementation/structure.md)
