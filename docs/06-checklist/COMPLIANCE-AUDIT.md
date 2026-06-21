# Аудит соответствия критическим правилам

Дата: 2026-06-03

## Правило 1: Направление зависимостей (PCMEF)

| Проверка | Статус |
|----------|--------|
| `@RestController` → только `@Service` | ✅ `SummonerController` → `SummonerService`, `MatchService` |
| `@Service` → только `@Repository` + `RiotApiClient` | ✅ |
| Репозиторий → сервис | ✅ Нет обратных вызовов |
| Entity → БД / Riot | ✅ Сущности без JPA-репозиториев внутри |

## Правило 2: GlobalExceptionHandler

| Проверка | Статус |
|----------|--------|
| `@RestControllerAdvice` | ✅ `GlobalExceptionHandler` |
| `SummonerNotFoundException` → 404 JSON | ✅ |
| `RiotRateLimitException` → 429 JSON | ✅ |
| `RiotApiException` → понятный JSON | ✅ |
| Stack trace клиенту | ✅ Скрыт (`include-exception: false`) |

## Правило 3: TypeScript без `any`

| Проверка | Статус |
|----------|--------|
| Интерфейсы `SummonerDTO`, `MatchDTO` | ✅ `mobile/src/types/api.ts` |
| `strict` + `noImplicitAny` | ✅ `mobile/tsconfig.json` |
| Axios типизирован | ✅ `get<SummonerDTO>`, `get<MatchDTO[]>` |

## Правило 4: Секреты

| Проверка | Статус |
|----------|--------|
| `riot.api.key: ${RIOT_API_KEY}` | ✅ `application.yml` |
| Ключ в коде | ✅ Отсутствует |
| Заголовок `X-Riot-Token` | ✅ `RiotApiConfig` / `RestClient` |

## Правило 5: UI React Native

| Проверка | Статус |
|----------|--------|
| Матчи через `FlatList` | ✅ `ProfileScreen.tsx` |
| Не `ScrollView` + `.map()` | ✅ |
| Очистка поля ввода | ✅ `SearchInput` |
| Валидация пустой строки | ✅ `SearchScreen`, `client.ts` |
| JWT в Axios | ✅ `api/client.ts` |
| Login / Favourites | ✅ `LoginScreen`, `FavouritesScreen` |

## Шаги интеграции Riot API (Mediator)

| Шаг | Статус |
|-----|--------|
| 1. `RiotApiConfig` + `RiotApiClient` (RestClient, X-Riot-Token) | ✅ |
| 2. Cache-Aside TTL 10 мин в `SummonerService` | ✅ |
| 3. `MatchService` — ID → кэш БД → парсинг → DTO | ✅ |
| 4. 429 → `RiotRateLimitException` + handler | ✅ |

## Исправленные замечания

| # | Было | Стало |
|---|------|--------|
| 1 | Java 21 в `pom.xml` | **Java 17** (`<java.version>17</java.version>`) |
| 2 | Нет tier/LP из League API | **`getLeagueEntriesByPuuid`** + `SummonerService.applyLeagueEntries` |
| 3 | Жёсткий URL `10.0.2.2` | **`resolveApiBaseUrl()`** + `EXPO_PUBLIC_API_URL` (см. `mobile/.env.example`) |
