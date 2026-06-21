# Интерфейсы системы

## Внешние интерфейсы

| Интерфейс | Протокол | Назначение |
|-----------|----------|------------|
| Mobile ↔ Backend | HTTPS REST + JSON | CRUD, поиск, избранное, JWT |
| Backend ↔ Riot API | HTTPS REST | Account-V1, League-V4, Match-V5 |
| Backend ↔ PostgreSQL | JDBC | Персистентность, кэш |

## REST API (Presentation → Control)

Базовый путь: `/api`

| Группа | Префикс | Аутентификация |
|--------|---------|----------------|
| Auth | `/auth/*` | Публичный |
| Summoner | `/summoner/*` | JWT (кроме search — опционально) |
| User | `/user/*` | JWT |

Полный перечень: [09-api/REST-ENDPOINTS.md](../09-api/REST-ENDPOINTS.md)

## Внутренние интерфейсы (PCMEF)

```
@RestController  →  @Service  →  @Repository
                      ↓
                 RiotApiClient (HTTP)
```

| Слой | Интерфейс | Реализация |
|------|-----------|------------|
| Control | REST controllers | `AuthController`, `SummonerController`, `UserController` |
| Mediator | Service interfaces | `SummonerService`, `AuthService`, `MatchService` |
| Foundation | JPA repositories | `UserRepository`, `SummonerRepository`, … |
| Foundation | Riot client | `RiotApiClient` |

## Мобильный клиент

| Модуль | Интерфейс |
|--------|-----------|
| API | `mobile/src/api/client.ts` — axios + interceptors |
| State | Zustand stores (`authStore`, `summonerStore`) |
| Storage | AsyncStorage — токен, кэш профилей |

## Диаграмма компонентов

![Пакеты PCMEF](../images/fig-04-packages.png)

Рисунок 4 — Структура пакетов backend (PCMEF)
